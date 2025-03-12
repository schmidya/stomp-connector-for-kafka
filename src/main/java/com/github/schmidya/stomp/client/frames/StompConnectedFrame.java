package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompConnectedFrame extends StompServerFrame {

    protected StompConnectedFrame(Map<String, String> headers) {
        super(CMD_CONNECTED, headers, "");
        // TODO Auto-generated constructor stub
    }

}
