package com.github.schmidya.stomp.client.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class StompSessionTest {

    @Test
    public void parseTcpUrl(){
        try {
            StompSession session = StompSession.fromUrl("tcp://host:1234");
            assertInstanceOf(TransportLayerSession.class, session);
            if (session instanceof TransportLayerSession ts){
                assertEquals("host", ts.hostname);
                assertEquals(1234, ts.port);
                assertFalse(ts.ssl);
            }
        } catch(IOException e){
            fail();
        }
    }

    @Test
    public void parseSslUrl(){
        try {
            StompSession session = StompSession.fromUrl("ssl://host:1234");
            assertInstanceOf(TransportLayerSession.class, session);
            if (session instanceof TransportLayerSession ts){
                assertEquals("host", ts.hostname);
                assertEquals(1234, ts.port);
                assertTrue(ts.ssl);
            }
        } catch(IOException e){
            fail();
        }
    }

    @Test
    public void parseWsUrl(){
        try{
            StompSession session = StompSession.fromUrl("ws://host:1234");
            assertInstanceOf(WebsocketSession.class, session);
        } catch(IOException e){
            fail();
        }
    }
    
}
