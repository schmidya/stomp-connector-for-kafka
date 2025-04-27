package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.WebSocketContainer;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

public class WebsocketSession implements StompSession {
    private static final Logger log = LoggerFactory.getLogger(WebsocketSession.class);

    public String url;

    private Session session;
    private WebSocketContainer webSocketContainer;
    private StompWebsocketListener listener;
    private StompServerFrameQueue serverFrameQueue;

    public WebsocketSession(
            String url,
            WebSocketContainer webSocketContainer,
            StompWebsocketListener listener,
            StompServerFrameQueue serverFrameQueue) {
        this.url = url;
        this.listener = listener;
        this.webSocketContainer = webSocketContainer;
        this.serverFrameQueue = serverFrameQueue;
    }

    @Override
    public StompServerFrameQueue getServerFrameQueue() {
        return serverFrameQueue;
    }

    @Override
    public void sendFrame(StompClientFrame frame) throws IOException {
        log.error("SENDING FRAME OVER WEBSOCKET");
        session.getBasicRemote().sendText(frame.toString());
    }

    @Override
    public StompServerFrame connect(StompConnectFrame frame) throws IOException {
        log.error("ATTEMPTING WEBSOCKET CONNECTION");
        listener.setServerFrameQueue(serverFrameQueue);
        try {
            session = webSocketContainer.connectToServer(listener, URI.create(url));
        } catch (DeploymentException e) {
            log.error(e.getMessage());
            throw new IOException(e.getMessage());
        } catch (ServiceConfigurationError err) {
            log.error(err.getMessage());
            throw err;
        }
        sendFrame(frame);

        while (serverFrameQueue.checkError() == null && serverFrameQueue.checkConnected() == null) {
            try {
                serverFrameQueue.wait();
            } catch (InterruptedException e) {

            }
        }

        if (serverFrameQueue.checkError() != null)
            return serverFrameQueue.checkError();
        return serverFrameQueue.checkConnected();
    }

}
