package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompMessageFrame;
import com.github.schmidya.stomp.client.frames.StompReceiptFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;
import com.github.schmidya.stomp.client.frames.StompSubscribeFrame;

public class StompClient {

    private static final Logger log = LoggerFactory.getLogger(StompClient.class);
    
    private String hostname;
    private int port;
    private Socket sk;
    private ConcurrentLinkedQueue<StompServerFrame> frames;
    Thread listenerThread;

    public StompClient(String hostname, int port){
        this.hostname=hostname;
        this.port=port;
        frames = new ConcurrentLinkedQueue<>();
    }

    public StompConnectedFrame connect(String login, String passcode) throws IOException {
        sk = new Socket(hostname,port);

        StompListener ls = new StompListener(sk.getInputStream(), frames);
        listenerThread = new Thread(ls);
        listenerThread.start();

        if (sk.isConnected()){
            sk.getOutputStream().write(new StompConnectFrame(hostname+":"+Integer.toString(port), login, passcode).toString().getBytes());
            StompServerFrame frame = getNextFrame();
            if (frame instanceof StompConnectedFrame ret) return ret;
            else {
                log.error(frame.toString());
            }
        }
        return null;
    }

    public StompReceiptFrame subscribe(String destination) throws IOException {
        String clientId = "test";

        StompSubscribeFrame f = new StompSubscribeFrame(clientId, destination, "sub-test");
        sk.getOutputStream().write(f.toString().getBytes());
        StompServerFrame r = getNextFrame();
        if (r instanceof StompReceiptFrame rct){
            log.error(rct.toString());
            return rct;
        } else {
            log.error(r.toString());
        }
        
        return null;
    }

    public List<StompMessageFrame> poll(){
        List<StompMessageFrame> ret = new ArrayList<>();
        StompServerFrame f = null;
        do {
            f = frames.poll();
            if (f instanceof StompMessageFrame m) ret.add(m);
            // TODO: treat ERROR frames etc.
        } while (f!=null);
        return ret;
    }

    public StompServerFrame getNextFrame() {
        StompServerFrame ret = null;
        do {
            ret = frames.poll();
        } while (ret==null); 
        return ret;
    }

}
