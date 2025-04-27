package com.github.schmidya.stomp.client.session;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;

@ClientEndpoint
public class StompWebsocketListener {
    private static final Logger log = LoggerFactory.getLogger(StompWebsocketListener.class);

    private StompServerFrameQueue Q;

    public void setServerFrameQueue(StompServerFrameQueue Q) {
        if (this.Q != null)
            throw new IllegalArgumentException("server frame queue already set");
        this.Q = Q;
    }

    @OnMessage
    public void receiveFrame(byte[] message, Session session) {
        StompServerFrame frame = StompServerFrame.fromString(new String(message, StandardCharsets.UTF_8));
        Q.add(frame);
    }

    // @OnError
    // public void handleError(Session session, Throwable throwable){

    // }

    // @OnOpen
    // public void onOpen(Session session, EndpointConfig config){

    // }

    // @OnClose
    // public void onClose(Session session, CloseReason reason){

    // }
}
