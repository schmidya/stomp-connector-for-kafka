package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

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

public class StompClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StompClient.class);

    private StompSession session;
    private Queue<StompAckFrame> ackFrames;
    private long receiptId;

    public StompClient(StompSession session) {
        this.session = session;
        ackFrames = new LinkedBlockingDeque<>();
        receiptId = 0;
    }

    public static StompClient fromUrl(String url) throws MalformedURLException {
        return new StompClient(StompSession.fromUrl(url));
    }

    public void write(StompClientFrame frame) throws IOException {
        session.sendFrame(frame);
    }

    public void sendMessage(String message, String destination) {
        try {
            write(new StompSendFrame(message, destination, "text/plain", "" + receiptId++));
        } catch (IOException e) {
            log.error("Exception while trying to send frame to broker: " + e.getMessage());
        }
    }

    public StompConnectedFrame connect(StompConnectFrame frame) throws IOException {
        StompServerFrame response = session.connect(frame);
        log.info("Established connection to broker:\n" + frame.toString());
        log.info("Response from broker " + response.toString());
        if (response instanceof StompConnectedFrame ret)
            return ret;
        else if (response instanceof StompErrorFrame err) {
            throw new IOException("Received error frame from STOMP broker:\n" + err.toString());
        }
        throw new IOException("Received invalid response from STOMP broker");
    }

    public StompReceiptFrame subscribe(String destination) throws IOException {
        String clientId = "test";

        StompSubscribeFrame f = new StompSubscribeFrame(clientId, destination, "sub-test");
        session.sendFrame(f);
        StompServerFrame r = session.getServerFrameQueue().waitForReceipt("sub-test");
        if (r instanceof StompReceiptFrame rct) {
            log.info("Obtained receipt for topic subscription:\n" + rct.toString());
            return rct;
        } else {
            log.error("Unexpected response from Server for subscription:\n" + r.toString());
        }

        return null;
    }

    public List<StompMessageFrame> poll() {
        // ack the previously polled messages
        for (StompAckFrame af = ackFrames.poll(); af != null; af = ackFrames.poll()) {
            try {
                session.sendFrame(af);
            } catch (Exception e) {
                log.error("Exception when ack-ing messages to broker. Sent frame:\n" + af.toString() + "\nException:\n"
                        + e.toString());
            }
        }

        List<StompMessageFrame> ret = new ArrayList<>();
        StompMessageFrame f = null;
        do {
            f = session.getServerFrameQueue().getReceivedMessages().poll();
            if (f != null) {
                ret.add(f);
                // ack_frames.add(new StompAckFrame(f.getAckId()));
            }
        } while (f != null);
        if (session.getServerFrameQueue().checkError() != null)
            log.error(session.getServerFrameQueue().checkError().toString());
        return ret;
    }

    public boolean checkConnectionHealth() {
        return session.checkConnectionHealth();
    }

    @Override
    public void close() throws Exception {
        session.close();
    }

}
