package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public abstract class StompClientFrame extends StompFrame {
    
    protected StompClientFrame(String command, Map<String,String> headers){
        super(command, headers);
    }

    
}
