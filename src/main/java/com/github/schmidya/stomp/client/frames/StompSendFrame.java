package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompSendFrame extends StompClientFrame {

    public StompSendFrame(String body, String destination, String contentType) {
        super("SEND", Map.of("destination", destination, "content-type", contentType, "content-length",
                "" + body.length(), "receipt", "test"), body);
        // TODO Auto-generated constructor stub
    }

}
