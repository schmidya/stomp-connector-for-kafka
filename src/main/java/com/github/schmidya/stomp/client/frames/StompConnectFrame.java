package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompConnectFrame extends StompClientFrame {

    public StompConnectFrame(String host, String login, String passcode) {
        super("CONNECT", Map.of("host", host, "login", login, "passcode", passcode, "accept-version", "1.2",
                "heart-beat", "" + 100 + "," + 100));
    }

    public int brokerHeartbeat() {
        try {
            var heartbeat = getHeader("heart-beat");
            return Integer.parseInt(heartbeat.split(",")[1]);
        } catch (Exception e) {

        }
        return 0;
    }

    public int clientHeartbeat() {
        try {
            var heartbeat = getHeader("heart-beat");
            return Integer.parseInt(heartbeat.split(",")[0]);
        } catch (Exception e) {

        }
        return 0;
    }

}
