package com.github.schmidya.stomp.client.session;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class StompWebsocketListener {
    private static final Logger logger = LoggerFactory.getLogger(StompWebsocketListener.class);

    private final AtomicLong lastMessage = new AtomicLong();
    private long heartbeat;
    private final AtomicInteger missedHeartbeats = new AtomicInteger(0);
    private final AtomicBoolean reachedEndOfStream = new AtomicBoolean(false);
    private final AtomicReference<Throwable> wsError = new AtomicReference<>();

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> heartbeatCounter;

    private StompServerFrameQueue Q;

    public void setServerFrameQueue(StompServerFrameQueue Q) {
        if (this.Q != null)
            throw new IllegalArgumentException("server frame queue already set");
        this.Q = Q;
    }

    public void startHeartbeatMonitor(long heartbeat) {
        lastMessage.set(System.currentTimeMillis());
        this.heartbeat = heartbeat;
        heartbeatCounter = scheduler.scheduleAtFixedRate(() -> {
            logger.debug("Checking heartbeat inteval");
            if (System.currentTimeMillis() - lastMessage.get() > this.heartbeat) {
                int x = missedHeartbeats.incrementAndGet();
                logger.info("Missed heartbeat. Total: " + x);
            }
        }, heartbeat, heartbeat, TimeUnit.MILLISECONDS);
    }

    public boolean checkConnectionHealth() {
        return wsError.get() == null && missedHeartbeats.get() == 0 && !reachedEndOfStream.get();
    }

    @OnMessage
    public void receiveFrame(byte[] message, Session session) {
        lastMessage.set(System.currentTimeMillis());
        var messageString = new String(message, StandardCharsets.UTF_8);
        if (!messageString.equals("\n")) { // ignore heartbeat messages
            StompServerFrame frame = StompServerFrame.fromString(messageString);
            Q.add(frame);
        }
    }

    @OnError
    public void handleError(Session session, Throwable throwable) {
        logger.error(throwable.toString());
        wsError.set(throwable);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (heartbeatCounter != null)
            heartbeatCounter.cancel(true);
        scheduler.shutdown();
        reachedEndOfStream.set(false);
    }
}
