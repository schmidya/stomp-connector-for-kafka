package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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
    private Deque<StompServerFrame> frames;
    private String incompleteFrame;

    public StompClient(String hostname, int port){
        this.hostname=hostname;
        this.port=port;
        this.frames=new LinkedList<StompServerFrame>();
    }

    public StompConnectedFrame connect(String login, String passcode) throws IOException {
        sk = new Socket(hostname,port);
        
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

    public StompMessageFrame poll() throws IOException{
        while (true) {
            StompServerFrame f = getNextFrame();
            if (f instanceof StompMessageFrame m) {
                return m;
            }
        }

    }
    
    private void listen() throws IOException{
        String frame = "";
        log.error("Listening on socket");
        while (true){
            int read = sk.getInputStream().read();
            if (read < 0) continue;
            byte b = (byte)read;
            if (b==0) break;
            frame += (char)b;
        }
        log.error(frame);
        
        frames.add(StompServerFrame.fromString(frame));
    }

    public StompServerFrame getNextFrame() throws IOException{
        while (frames.peekFirst()==null) listen();
        return frames.pop();
    }

}
