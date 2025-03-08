package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompMessageFrame extends StompServerFrame {

    protected StompMessageFrame(Map<String, String> headers, String body) {
        super(CMD_MESSAGE, headers, body);
        //TODO Auto-generated constructor stub
    }
    
}
