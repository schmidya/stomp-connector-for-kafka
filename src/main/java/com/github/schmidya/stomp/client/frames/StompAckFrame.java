package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompAckFrame extends StompClientFrame {

    public StompAckFrame(String ackId) {
        super(CMD_ACK, Map.of("id", ackId));
    }

}
