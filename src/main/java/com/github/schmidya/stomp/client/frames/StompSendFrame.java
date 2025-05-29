package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompSendFrame extends StompClientFrame {

    public StompSendFrame(String body, String destination, String contentType, String receiptId) {
        super("SEND", Map.of("destination", destination, "content-type", contentType, "content-length",
                "" + body.length(), "receipt", receiptId), body);
        // TODO Auto-generated constructor stub
    }

}
