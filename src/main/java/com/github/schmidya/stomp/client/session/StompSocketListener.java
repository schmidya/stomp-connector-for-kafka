package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class StompSocketListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StompSocketListener.class);

    InputStream is;
    StompServerFrameQueue Q;
    private final AtomicInteger missedHeartbeats = new AtomicInteger(0);
    private final AtomicBoolean reachedEndOfStream = new AtomicBoolean(false);
    private final AtomicBoolean stopSignal = new AtomicBoolean(false);

    public void setInputStream(InputStream is) {
        this.is = is;
    }

    public void setServerFrameQueue(StompServerFrameQueue Q) {
        if (this.Q != null)
            throw new IllegalArgumentException("server frame queue already set");
        this.Q = Q;
    }

    public boolean checkConnectionHealth() {
        return missedHeartbeats.get() == 0 && !reachedEndOfStream.get();
    }

    public void signalStop() {
        stopSignal.set(false);
    }

    @Override
    public void run() {
        String frame = "";
        int read = -1;
        while (!stopSignal.get()) {
            try {
                read = is.read();
            } catch (SocketTimeoutException e) {
                int x = missedHeartbeats.incrementAndGet();
                log.info("Missed heartbeat. Total: " + x);
            } catch (IOException e) {
                log.error(e.toString());
                log.error("Listener exiting");
                break;
            }
            if (read < 0) {
                reachedEndOfStream.set(true);
                break;
            }
            byte b = (byte) read;
            frame += (char) b;
            if (b == 0) {
                log.info(frame);
                var f = StompServerFrame.fromString(frame);
                log.info(f.getClass().toString());
                Q.add(StompServerFrame.fromString(frame));
                frame = "";
            }
        }
    }

}
