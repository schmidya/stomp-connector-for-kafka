package com.github.schmidya.stomp.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompErrorFrame;
import com.github.schmidya.stomp.client.frames.StompMessageFrame;
import com.github.schmidya.stomp.client.frames.StompReceiptFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class StompServerFrameQueue {

    private ConcurrentLinkedQueue<StompMessageFrame> messages;
    private ConcurrentLinkedQueue<StompReceiptFrame> receipts;
    private StompConnectedFrame connectedFrame;
    private StompErrorFrame errorFrame;

    public StompServerFrameQueue() {
        messages = new ConcurrentLinkedQueue<StompMessageFrame>();
        receipts = new ConcurrentLinkedQueue<StompReceiptFrame>();
    }

    public void add(StompServerFrame frame) {
        if (frame instanceof StompMessageFrame m)
            messages.add(m);
        else if (frame instanceof StompReceiptFrame r)
            receipts.add(r);
        else if (frame instanceof StompErrorFrame e)
            errorFrame = e;
        else if (frame instanceof StompConnectedFrame c)
            connectedFrame = c;
    }

    public StompReceiptFrame waitForReceipt(String receiptId) throws IOException {
        while (true) {
            if (checkError() != null) {
                throw new IOException("Received error frame from STOMP broker:\n" + checkError().toString());
            }
            Iterator<StompReceiptFrame> it = receipts.iterator();
            while (it.hasNext()) {
                StompReceiptFrame f = it.next();
                if (f.getHeader("receipt-id").equals(receiptId)) {
                    receipts.remove(f);
                    return f;
                }
            }
        }
    }

    public Queue<StompMessageFrame> getReceivedMessages() {
        return messages;
    }

    public Queue<StompReceiptFrame> getReceipts() {
        return receipts;
    }

    public StompErrorFrame checkError() {
        return errorFrame;
    }

    public StompConnectedFrame checkConnected() {
        return connectedFrame;
    }

}
