package com.github.schmidya.stomp.client.frames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

public class StompServerFrameTest {

    @Test
    public void parseConnectedFrame() {
        StompServerFrame frame = StompServerFrame.fromString(
                "" +
                        "CONNECTED\n" +
                        "header1:value1\n" +
                        "header2:value2\n" +
                        "\n" +
                        "\n" + (char) 0);
        assertInstanceOf(StompConnectedFrame.class, frame);
        assertEquals("value1", frame.getHeader("header1"));
        assertEquals("value2", frame.getHeader("header2"));
        assertEquals("", frame.getBody());
    }

    @Test
    public void parseReceiptFrame() {
        StompServerFrame frame = StompServerFrame.fromString(
                "" +
                        "RECEIPT\n" +
                        "header1:value1\n" +
                        "header2:value2\n" +
                        "\n" +
                        "\n" + (char) 0);
        assertInstanceOf(StompReceiptFrame.class, frame);
        assertEquals("value1", frame.getHeader("header1"));
        assertEquals("value2", frame.getHeader("header2"));
        assertEquals("", frame.getBody());
    }

    @Test
    public void parseMessageFrame() {
        StompServerFrame frame = StompServerFrame.fromString(
                "" +
                        "MESSAGE\n" +
                        "header1:value1\n" +
                        "header2:value2\n" +
                        "\n" +
                        "bodyline1\n" +
                        "bodyline2\n" +
                        "\n" + (char) 0);
        assertInstanceOf(StompMessageFrame.class, frame);
        assertEquals("value1", frame.getHeader("header1"));
        assertEquals("value2", frame.getHeader("header2"));
        assertEquals("bodyline1\nbodyline2\n\n", frame.getBody());
    }

    @Test
    public void parseErrorFrame() {
        StompServerFrame frame = StompServerFrame.fromString(
                "" +
                        "ERROR\n" +
                        "header1:value1\n" +
                        "header2:value2\n" +
                        "\n" +
                        "bodyline1\n" +
                        "bodyline2\n" +
                        "\n" + (char) 0);
        assertInstanceOf(StompErrorFrame.class, frame);
        assertEquals("value1", frame.getHeader("header1"));
        assertEquals("value2", frame.getHeader("header2"));
        assertEquals("bodyline1\nbodyline2\n\n", frame.getBody());

    }
}
