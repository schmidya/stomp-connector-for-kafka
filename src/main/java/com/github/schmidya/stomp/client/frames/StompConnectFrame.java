package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompConnectFrame extends StompClientFrame {
    

    public StompConnectFrame(String host, String login, String passcode){
        super("CONNECT", Map.of("host",host,"login",login,"passcode",passcode,"accept-version","1.2"));
    }

}
