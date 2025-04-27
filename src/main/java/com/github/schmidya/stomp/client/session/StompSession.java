package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

import jakarta.websocket.ContainerProvider;

public interface StompSession {

    public StompServerFrameQueue getServerFrameQueue();

    public void sendFrame(StompClientFrame frame) throws IOException;

    public StompServerFrame connect(StompConnectFrame frame) throws IOException;

    public static StompSession fromUrl(String url) throws MalformedURLException {

        Pattern pattern = Pattern.compile("(wss?|ssl|tcp)://([a-zA-Z0-9.-]+):([0-9]{1,5})");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new MalformedURLException("URL string must specify protocol, host, and port explicitely.");
        }
        String protocol = matcher.group(1);
        String hostname = matcher.group(2);
        int port = Integer.parseInt(matcher.group(3));

        StompServerFrameQueue Q = new StompServerFrameQueue();

        switch (protocol) {
            case "tcp":
                return new TransportLayerSession(hostname, port, SocketFactory.getDefault(), new StompSocketListener(),
                        Q);
            case "ssl":
                return new TransportLayerSession(hostname, port, SSLSocketFactory.getDefault(),
                        new StompSocketListener(), Q);

            case "ws":
            case "wss":
                return new WebsocketSession(url, ContainerProvider.getWebSocketContainer(),
                        new StompWebsocketListener(), Q);
            default:
                throw new MalformedURLException(); // Should actually be unreachable
        }

    }

}
