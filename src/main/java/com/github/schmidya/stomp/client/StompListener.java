package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class StompListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StompListener.class);

    InputStream is;
    ConcurrentLinkedQueue<StompServerFrame> Q;

    public StompListener(InputStream is, ConcurrentLinkedQueue<StompServerFrame> Q){
        this.is = is;
        this.Q = Q;
    }

    @Override
    public void run() {
        String frame = "";
        int read = -1;
        while (true){
            try{
                read = is.read();
            } catch (IOException e){
                log.error(e.toString());
                log.error("Listener exiting");
                return;
            }
            if (read < 0) continue;
            byte b = (byte)read;
            if (b==0) {
                Q.add(StompServerFrame.fromString(frame));
                frame = "";
            } else {
                frame += (char)b;
            }
        }
    }
    
}
