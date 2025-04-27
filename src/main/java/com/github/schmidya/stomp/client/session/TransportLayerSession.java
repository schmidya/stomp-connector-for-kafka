package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.Socket;
import javax.net.SocketFactory;
import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class TransportLayerSession implements StompSession {

    private String hostname;
    private int port;
    private SocketFactory socketFactory;

    private Socket sk;
    private Thread listenerThread;
    private StompSocketListener listener;
    private StompServerFrameQueue serverFrameQueue;

    public TransportLayerSession(
            String hostname,
            int port,
            SocketFactory socketFactory,
            StompSocketListener listener,
            StompServerFrameQueue serverFrameQueue) {
        this.hostname = hostname;
        this.port = port;
        this.listener = listener;
        this.socketFactory = socketFactory;

        this.serverFrameQueue = serverFrameQueue;

    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public SocketFactory getSocketFactory() {
        return socketFactory;
    }

    @Override
    public void sendFrame(StompClientFrame frame) throws IOException {
        sk.getOutputStream().write(frame.toString().getBytes());
    }

    @Override
    public StompServerFrame connect(StompConnectFrame frame) throws IOException {
        sk = socketFactory.createSocket(hostname, port);

        listener.setInputStream(sk.getInputStream());
        listener.setServerFrameQueue(serverFrameQueue);

        listenerThread = new Thread(listener);
        listenerThread.start();

        sendFrame(frame);

        return serverFrameQueue.waitForConnected();
    }

    @Override
    public StompServerFrameQueue getServerFrameQueue() {
        return serverFrameQueue;
    }

}
