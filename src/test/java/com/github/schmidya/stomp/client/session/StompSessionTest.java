package com.github.schmidya.stomp.client.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class StompSessionTest {

    @Test
    public void parseTcpUrl() {
        try {
            StompSession session = StompSession.fromUrl("tcp://host:1234");
            assertInstanceOf(TransportLayerSession.class, session);
            if (session instanceof TransportLayerSession ts) {
                assertEquals("host", ts.getHostname());
                assertEquals(1234, ts.getPort());
                assertInstanceOf(SocketFactory.class, ts.getSocketFactory());
                assertFalse(ts.getSocketFactory() instanceof SSLSocketFactory);
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void parseSslUrl() {
        try {
            StompSession session = StompSession.fromUrl("ssl://host:1234");
            assertInstanceOf(TransportLayerSession.class, session);
            if (session instanceof TransportLayerSession ts) {
                assertEquals("host", ts.getHostname());
                assertEquals(1234, ts.getPort());
                assertInstanceOf(SSLSocketFactory.class, ts.getSocketFactory());
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void parseWsUrl() {
        try {
            StompSession session = StompSession.fromUrl("ws://host:1234");
            assertInstanceOf(WebsocketSession.class, session);
        } catch (IOException e) {
            fail();
        }
    }

}
