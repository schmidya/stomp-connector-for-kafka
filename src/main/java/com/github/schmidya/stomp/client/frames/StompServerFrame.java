package com.github.schmidya.stomp.client.frames;

import java.util.HashMap;
import java.util.Map;

public abstract class StompServerFrame extends StompFrame {

    protected static final String CMD_CONNECTED = "CONNECTED";
    protected static final String CMD_ERROR = "ERROR";
    protected static final String CMD_RECEIPT = "RECEIPT";
    protected static final String CMD_MESSAGE = "MESSAGE";

    protected StompServerFrame(String command, Map<String, String> headers, String body) {
        super(command, headers, body);
    }

    public static StompServerFrame fromString(String s) {

        Object[] lines = s.lines().toArray();
        Map<String, String> headers = new HashMap<>();
        int idx = 0;
        String command = "";

        while (idx < lines.length) {
            command = (String) lines[idx++];
            if (command.length() > 0)
                break;
        }

        while (idx < lines.length) {
            String headerLine = (String) lines[idx++];
            if (headerLine.equals(""))
                break;
            String[] splits = headerLine.split(":");
            if (splits.length != 2) {
                break; // TODO: throw exception
            }
            headers.put(splits[0], splits[1]);
        }

        String body = "";
        while (idx < lines.length) {
            body += lines[idx++] + "\n";
        }

        return fromData(command, headers, body);
    }

    public static StompServerFrame fromData(String command, Map<String, String> headers, String body) {
        switch (command) {
            case CMD_CONNECTED:
                return new StompConnectedFrame(headers);
            case CMD_ERROR:
                return new StompErrorFrame(headers, body);
            case CMD_MESSAGE:
                return new StompMessageFrame(headers, body);
            case CMD_RECEIPT:
                return new StompReceiptFrame(headers);
            default:
                return null;
        }
    }
}
