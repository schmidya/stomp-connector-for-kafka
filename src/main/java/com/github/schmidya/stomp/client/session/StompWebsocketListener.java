package com.github.schmidya.stomp.client.session;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(StompWebsocketListener.class);

    private ConcurrentLinkedQueue<StompServerFrame> receivedFrames;

    public StompWebsocketListener(ConcurrentLinkedQueue<StompServerFrame> receivedFrames){
        this.receivedFrames = receivedFrames;
    }
    
    @OnMessage
    public void receiveFrame(byte[] message, Session session){
        StompServerFrame frame = StompServerFrame.fromString(new String(message, StandardCharsets.UTF_8));
        receivedFrames.add(frame);
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
