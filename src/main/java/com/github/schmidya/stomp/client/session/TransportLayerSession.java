package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLSocketFactory;

import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class TransportLayerSession implements StompSession {

    public String hostname;
    public int port;
    public boolean ssl;


    private Socket sk;
    private Thread listenerThread;
    private StompSocketListener listener;
    private ConcurrentLinkedQueue<StompServerFrame> receivedFrames;

    public TransportLayerSession(
        String hostname,
        int port,
        boolean ssl
    ){
        this.hostname = hostname;
        this.port = port;
        this.ssl = ssl;

        receivedFrames = new ConcurrentLinkedQueue<StompServerFrame>();

    }
    
    @Override
    public ConcurrentLinkedQueue<StompServerFrame> getReceivedFrameQueue() {
        return receivedFrames;
    }
    
    @Override
    public void sendFrame(StompClientFrame frame) throws IOException {
        sk.getOutputStream().write(frame.toString().getBytes());
    }
    
    @Override
    public void connect() throws IOException {
        if (ssl){
            sk = SSLSocketFactory.getDefault().createSocket(hostname, port);
        } else {
            sk = new Socket(hostname, port);
        }
        
        listener = new StompSocketListener(sk.getInputStream(), receivedFrames);
        listenerThread = new Thread(listener);
        listenerThread.start();
    }
    
}
