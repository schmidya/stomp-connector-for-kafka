package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class StompSocketListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StompSocketListener.class);

    InputStream is;
    StompServerFrameQueue Q;

    public void setInputStream(InputStream is) {
        if (this.is != null)
            throw new IllegalArgumentException("server frame queue already set");
        this.is = is;
    }

    public void setServerFrameQueue(StompServerFrameQueue Q) {
        if (this.Q != null)
            throw new IllegalArgumentException("server frame queue already set");
        this.Q = Q;
    }

    @Override
    public void run() {
        String frame = "";
        int read = -1;
        while (true) {
            try {
                read = is.read();
            } catch (IOException e) {
                log.error(e.toString());
                log.error("Listener exiting");
                return;
            }
            if (read < 0)
                continue;
            byte b = (byte) read;
            frame += (char) b;
            if (b == 0) {
                Q.add(StompServerFrame.fromString(frame));
                frame = "";
            }
        }
    }

}
