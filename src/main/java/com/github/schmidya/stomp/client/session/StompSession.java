package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public interface StompSession {

    public ConcurrentLinkedQueue<StompServerFrame> getReceivedFrameQueue();

    public void sendFrame(StompClientFrame frame) throws IOException;

    public void connect() throws IOException;

    public static StompSession fromUrl(String url) throws IOException {

        Pattern pattern = Pattern.compile("(wss?|ssl|tcp)://([a-zA-Z0-9.-]+):([0-9]{1,5})");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new MalformedURLException("URL string must specify protocol, host, and port explicitely.");
        }
        String protocol = matcher.group(1);
        switch (protocol) {
            case "tcp":
            case "ssl":
                String hostname = matcher.group(2);
                int port = Integer.parseInt(matcher.group(3));
                return new TransportLayerSession(hostname, port, protocol.equals("ssl"));

            case "ws":
            case "wss":
                return new WebsocketSession(url);
            default:
                throw new MalformedURLException(); // Should actually be unreachable
        }

    }

}
