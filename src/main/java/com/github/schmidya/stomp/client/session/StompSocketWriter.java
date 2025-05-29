package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompClientFrame;

public class StompSocketWriter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(StompSocketWriter.class);

    OutputStream os;
    BlockingQueue<StompClientFrame> sendBuffer;
    int heartbeat;

    private final AtomicBoolean stopSignal = new AtomicBoolean(false);

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    public void setSendBuffer(BlockingQueue<StompClientFrame> sendBuffer) {
        if (this.sendBuffer != null)
            throw new IllegalArgumentException("send buffer queue already set");
        this.sendBuffer = sendBuffer;
    }

    public void signalStop() {
        stopSignal.set(true);
    }

    @Override
    public void run() {
        while (!stopSignal.get()) {
            var frame = sendBuffer.poll();
            if (frame == null) {
                try {
                    // send heartbeat
                    logger.debug("Sending heartbeat");
                    os.write('\n');
                } catch (IOException e) {
                    // TODO: error handling
                }
                // wait
                try {
                    Thread.sleep(heartbeat);
                } catch (InterruptedException e) {

                }
            } else {
                try {
                    // send frame
                    logger.debug("Sending frame");
                    os.write(frame.toString().getBytes());
                } catch (IOException e) {
                    // TODO: error handling
                }
            }
        }
    }

}
