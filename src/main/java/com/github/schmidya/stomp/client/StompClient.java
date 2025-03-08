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
import com.github.schmidya.stomp.client.frames.StompServerFrame;
import com.github.schmidya.stomp.connector.source.StompSourceTask;

public class StompClient {

    private static final Logger log = LoggerFactory.getLogger(StompSourceTask.class);
    
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
        }
        return null;
    }
    
    private void listen() throws IOException{
        String frame = "";
        while (true){
            byte b = (byte)sk.getInputStream().read();
            if (b==0) break;
            frame += (char)b;
        }
        
        frames.add(StompServerFrame.fromString(frame));
    }

    public StompServerFrame getNextFrame() throws IOException{
        while (frames.peekFirst()==null) listen();
        return frames.pop();
    }

}
