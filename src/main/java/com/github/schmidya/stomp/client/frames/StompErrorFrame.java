package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompErrorFrame extends StompServerFrame {

    protected StompErrorFrame(Map<String, String> headers, String body) {
        super(CMD_ERROR, headers, body);
        //TODO Auto-generated constructor stub
    }
    
}
