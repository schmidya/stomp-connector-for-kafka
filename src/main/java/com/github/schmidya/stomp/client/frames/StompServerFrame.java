package com.github.schmidya.stomp.client.frames;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        List<String> lines = s.lines().toList();
        Map<String, String> headers = new HashMap<>();

        Iterator<String> it = lines.iterator();

        String command = it.next();
        while (command.equals(""))
            command = it.next();

        while (it.hasNext()) {
            String headerLine = it.next();
            if (headerLine.equals(""))
                break;
            String[] splits = headerLine.split(":");
            if (splits.length != 2) {
                throw new IllegalArgumentException(
                        "Encounter malformed STOMP header: " + headerLine + "\nin the server frame:\n" + s);
            }
            headers.put(splits[0], splits[1]);
        }

        String body = "";

        while (it.hasNext()) {
            body += it.next();
            if (it.hasNext())
                body += "\n";
            else if (body.lastIndexOf(0) >= 0) {
                body = body.substring(0, body.lastIndexOf(0));
            }
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
