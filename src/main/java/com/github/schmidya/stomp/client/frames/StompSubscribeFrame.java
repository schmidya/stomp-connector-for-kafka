package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompSubscribeFrame extends StompClientFrame {
    public StompSubscribeFrame(String clientId, String destination, String receiptId){
        super("SUBSCRIBE", Map.of("client",clientId, "destination",destination, "receipt",receiptId));
    }
}
