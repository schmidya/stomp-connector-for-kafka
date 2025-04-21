package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

public class WebsocketSession implements StompSession {
    private static final Logger log = LoggerFactory.getLogger(WebsocketSession.class);

    public String url;

    private Session session;
    private StompWebsocketListener listener;
    private ConcurrentLinkedQueue<StompServerFrame> receivedFrames;

    public WebsocketSession(String url) {
        this.url = url;
        receivedFrames = new ConcurrentLinkedQueue<StompServerFrame>();
    }

    @Override
    public ConcurrentLinkedQueue<StompServerFrame> getReceivedFrameQueue() {
        return receivedFrames;
    }

    @Override
    public void sendFrame(StompClientFrame frame) throws IOException {
        log.error("SENDING FRAME OVER WEBSOCKET");
        session.getBasicRemote().sendText(frame.toString());
    }

    @Override
    public void connect() throws IOException {
        log.error("ATTEMPTING WEBSOCKET CONNECTION");
        try {
            listener = new StompWebsocketListener(receivedFrames);
            session = ContainerProvider.getWebSocketContainer().connectToServer(listener, URI.create(url));
        } catch (DeploymentException e) {
            log.error(e.getMessage());
            throw new IOException(e.getMessage());
        } catch (ServiceConfigurationError err) {
            log.error(err.getMessage());
            throw err;
        }
    }

}
