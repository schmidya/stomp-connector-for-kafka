package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.frames.StompAckFrame;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompErrorFrame;
import com.github.schmidya.stomp.client.frames.StompMessageFrame;
import com.github.schmidya.stomp.client.frames.StompReceiptFrame;
import com.github.schmidya.stomp.client.frames.StompSendFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;
import com.github.schmidya.stomp.client.frames.StompSubscribeFrame;
import com.github.schmidya.stomp.client.session.StompSession;

public class StompClient {

    private static final Logger log = LoggerFactory.getLogger(StompClient.class);

    private StompSession session;
    private Queue<StompAckFrame> ack_frames;

    public StompClient(StompSession session) {
        this.session = session;
        ack_frames = new LinkedBlockingDeque<>();
    }

    public static StompClient fromUrl(String url) throws MalformedURLException {
        return new StompClient(StompSession.fromUrl(url));
    }

    public void write(StompClientFrame frame) {
        // TODO: place to check connection health
        try {
            session.sendFrame(frame);
        } catch (IOException e) {

        }
    }

    public void sendMessage(String message, String destination) {
        write(new StompSendFrame(message, destination, "text/plain"));
    }

    public StompConnectedFrame connect(StompConnectFrame frame) throws IOException {
        StompServerFrame response = session.connect(frame);
        log.error(frame.toString());
        if (response instanceof StompConnectedFrame ret)
            return ret;
        else if (response instanceof StompErrorFrame err) {
            throw new IOException("Received error frame from STOMP broker:\n" + response.toString());
        }
        throw new IOException("Received invalid response from STOMP broker");
    }

    public StompReceiptFrame subscribe(String destination) throws IOException {
        String clientId = "test";

        StompSubscribeFrame f = new StompSubscribeFrame(clientId, destination, "sub-test");
        session.sendFrame(f);
        StompServerFrame r = session.getServerFrameQueue().waitForReceipt("sub-test");
        if (r instanceof StompReceiptFrame rct) {
            log.error(rct.toString());
            return rct;
        } else {
            log.error(r.toString());
        }

        return null;
    }

    public List<StompMessageFrame> poll() {
        // ack the previously polled messages
        for (StompAckFrame af = ack_frames.poll(); af != null; af = ack_frames.poll()) {
            try {
                session.sendFrame(af);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }

        List<StompMessageFrame> ret = new ArrayList<>();
        StompServerFrame f = null;
        do {
            f = session.getServerFrameQueue().getReceivedMessages().poll();
            if (f instanceof StompMessageFrame m) {
                ret.add(m);
                ack_frames.add(new StompAckFrame(m.getAckId()));
            } else {
                // TODO: treat ERROR frames etc.
            }
        } while (f != null);
        return ret;
    }

}
