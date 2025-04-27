package com.github.schmidya.stomp.client.session;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.SocketFactory;

import org.junit.jupiter.api.Test;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class TransportLayerSessionTest {

    @Test
    public void connectTest() {

        String rawConnectedFrame = "" +
                "CONNECTED\n" +
                "version:1.2\n\n" + (char) 0;
        StompConnectedFrame connectedFrame = null;
        if (StompServerFrame.fromString(rawConnectedFrame) instanceof StompConnectedFrame f)
            connectedFrame = f;
        else
            fail();

        String hostname = "test-host";
        int port = 1234;

        SocketFactory socketFactory = mock();
        StompSocketListener listener = mock();
        StompServerFrameQueue serverFrameQueue = mock();
        Socket socket = mock();
        InputStream is = mock();
        OutputStream os = mock();

        try {
            when(serverFrameQueue.checkConnected()).thenReturn(connectedFrame);
            when(socket.getOutputStream()).thenReturn(os);
            when(socket.getInputStream()).thenReturn(is);
            when(socketFactory.createSocket(hostname, port)).thenReturn(socket);

            TransportLayerSession session = new TransportLayerSession(
                    hostname, port,
                    socketFactory,
                    listener,
                    serverFrameQueue);
            session.connect(new StompConnectFrame(hostname, "login", "passcode"));
            verify(socketFactory).createSocket(hostname, port);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
