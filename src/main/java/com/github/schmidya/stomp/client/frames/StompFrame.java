package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public abstract class StompFrame {
    String command;
    Map<String, String> headers;
    String body;

    protected StompFrame(String command, Map<String, String> headers, String body) {
        this.command = command;
        this.headers = headers;
        this.body = body;
    }

    protected StompFrame(String command, Map<String, String> headers) {
        this(command, headers, "");
    }

    public String toString() {
        String frame = "";
        frame += command;
        frame += "\n";
        for (String header : headers.keySet()) {
            frame += header;
            frame += ":";
            frame += headers.get(header);
            frame += "\n";
        }
        frame += "\n";
        frame += body;
        frame += "\0";
        return frame;
    }
}
