package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceConfigurationError;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.WebSocketContainer;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

public class WebsocketSession implements StompSession {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketSession.class);

    public String url;

    private Session session;
    private WebSocketContainer webSocketContainer;
    private StompWebsocketListener listener;
    private StompWebsocketWriter writer;
    private Thread writerThread;
    private StompServerFrameQueue serverFrameQueue;
    private BlockingQueue<StompClientFrame> sendBuffer;

    public WebsocketSession(
            String url,
            WebSocketContainer webSocketContainer,
            StompWebsocketListener listener,
            StompWebsocketWriter writer,
            StompServerFrameQueue serverFrameQueue,
            BlockingQueue<StompClientFrame> sendBuffer) {
        this.url = url;
        this.listener = listener;
        this.writer = writer;
        this.webSocketContainer = webSocketContainer;
        this.serverFrameQueue = serverFrameQueue;
        this.sendBuffer = sendBuffer;
    }

    @Override
    public StompServerFrameQueue getServerFrameQueue() {
        return serverFrameQueue;
    }

    @Override
    public void sendFrame(StompClientFrame frame) throws IOException {
        sendBuffer.add(frame);
    }

    @Override
    public StompServerFrame connect(StompConnectFrame connectFrame) throws IOException {
        logger.info("Attempting WS connection");
        listener.setServerFrameQueue(serverFrameQueue);
        try {
            session = webSocketContainer.connectToServer(listener, URI.create(url));
        } catch (DeploymentException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        } catch (ServiceConfigurationError err) {
            logger.error(err.getMessage());
            throw err;
        }
        logger.trace("sending frame over websocket:\n" + connectFrame.toString());
        writer.setHeartbeat(10000);
        writer.setSendBuffer(sendBuffer);
        writer.setRemote(session.getBasicRemote());
        writerThread = new Thread(writer);
        writerThread.start();
        sendFrame(connectFrame);

        var frame = serverFrameQueue.waitForConnected();

        if (frame instanceof StompConnectedFrame connectedFrame) {
            // Start the heartbeat monitor. It will check the interval between messages.
            // Twice the negogiated value is used to provide some tolerance
            int negotiatedBrokerHeartbeat = Math.max(connectFrame.brokerHeartbeat(), connectedFrame.brokerHeartbeat());
            logger.info("Starting heartbeat monitor with twice the value of the negociated broker heartbeat: "
                    + 2 * negotiatedBrokerHeartbeat
                    + " ms");
            listener.startHeartbeatMonitor(2 * negotiatedBrokerHeartbeat);

            // set the writer heartbeat to half the client hartbeat
            // This will determine how long the writer thread will sleep if no client frames
            // are there to be sent
            logger.info("Setting heartbeat inteval for writer to half the negotiated STOMP heartbeat of the client: "
                    + negotiatedBrokerHeartbeat / 2 + " ms");
            int negotiatedClientHeartbeat = Math.max(connectFrame.clientHeartbeat(), connectedFrame.clientHeartbeat());
            writer.setHeartbeat(negotiatedClientHeartbeat);
        }

        return frame;
    }

    @Override
    public boolean checkConnectionHealth() {
        return session.isOpen() && listener.checkConnectionHealth();
    }

    @Override
    public void close() throws Exception {
        writer.signalStop();
        writerThread.join(1000);
        session.close();
    }

}
