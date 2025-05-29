package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompConnectedFrame extends StompServerFrame {

    protected StompConnectedFrame(Map<String, String> headers) {
        super(CMD_CONNECTED, headers, "");
        // TODO Auto-generated constructor stub
    }

    public int brokerHeartbeat() {
        try {
            var heartbeat = getHeader("heart-beat");
            return Integer.parseInt(heartbeat.split(",")[0]);
        } catch (Exception e) {

        }
        return 0;
    }

    public int clientHeartbeat() {
        try {
            var heartbeat = getHeader("heart-beat");
            return Integer.parseInt(heartbeat.split(",")[1]);
        } catch (Exception e) {

        }
        return 0;
    }

}
