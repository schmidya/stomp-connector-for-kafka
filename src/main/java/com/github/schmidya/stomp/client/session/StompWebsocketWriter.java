package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompClientFrame;

import jakarta.websocket.RemoteEndpoint.Basic;

public class StompWebsocketWriter implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StompWebsocketWriter.class);

    Basic remote;
    BlockingQueue<StompClientFrame> sendBuffer;
    int heartbeat;

    AtomicBoolean stopSignal = new AtomicBoolean(false);

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setRemote(Basic remote) {
        this.remote = remote;
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
                    log.debug("sending heartbeat");
                    // send heartbeat
                    remote.sendText("\n");
                } catch (IOException e) {
                    // TODO: error handling
                }
                // wait
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
            } else {
                try {
                    // send frame
                    remote.sendText(frame.toString());
                } catch (IOException e) {
                    // TODO: error handling
                }
            }
        }
    }
}
