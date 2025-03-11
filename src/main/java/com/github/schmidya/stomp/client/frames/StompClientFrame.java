package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public abstract class StompClientFrame extends StompFrame {

    protected static final String CMD_ACK = "ACK";
    protected static final String CMD_CONNECT = "CONNECT";
    
    protected StompClientFrame(String command, Map<String, String> headers, String body){
        super(command, headers, body);
    }

    protected StompClientFrame(String command, Map<String,String> headers){
        super(command, headers);
    }

    
}
