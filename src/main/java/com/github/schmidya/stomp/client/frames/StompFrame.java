package com.github.schmidya.stomp.client.frames;

import java.util.Map;
import java.util.Set;

public abstract class StompFrame {
    private final String command;
    private final Map<String, String> headers;
    private final String body;

    protected StompFrame(String command, Map<String, String> headers, String body) {
        this.command = command;
        this.headers = headers;
        this.body = body;
    }

    protected StompFrame(String command, Map<String, String> headers) {
        this(command, headers, "");
    }

    public String getBody() {
        return body;
    }

    public Set<String> getHeaderSet() {
        return headers.keySet();
    }

    public String getHeader(String header) {
        return headers.get(header);
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
