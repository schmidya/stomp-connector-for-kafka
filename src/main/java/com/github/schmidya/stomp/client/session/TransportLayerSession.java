package com.github.schmidya.stomp.client.session;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompServerFrameQueue;
import com.github.schmidya.stomp.client.frames.StompClientFrame;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

public class TransportLayerSession implements StompSession {
    Logger logger = LoggerFactory.getLogger(TransportLayerSession.class);

    private String hostname;
    private int port;
    private SocketFactory socketFactory;

    private Socket sk;
    private Thread listenerThread;
    private StompSocketListener listener;
    private StompSocketWriter writer;
    private Thread writerThread;
    private StompServerFrameQueue serverFrameQueue;
    private BlockingQueue<StompClientFrame> sendBuffer;

    public TransportLayerSession(
            String hostname,
            int port,
            SocketFactory socketFactory,
            StompSocketListener listener,
            StompSocketWriter writer,
            StompServerFrameQueue serverFrameQueue,
            BlockingQueue<StompClientFrame> sendBuffer) {
        this.hostname = hostname;
        this.port = port;
        this.listener = listener;
        this.writer = writer;
        this.socketFactory = socketFactory;

        this.serverFrameQueue = serverFrameQueue;
        this.sendBuffer = sendBuffer;

    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public SocketFactory getSocketFactory() {
        return socketFactory;
    }

    @Override
    public void sendFrame(StompClientFrame frame) {
        sendBuffer.add(frame);
    }

    @Override
    public StompServerFrame connect(StompConnectFrame connectFrame) throws IOException {
        sk = socketFactory.createSocket(hostname, port);
        logger.info("Setting initial socket timeout to 10s");
        sk.setSoTimeout(10000); // Set generous timeout to start with to catch any connection errors during
                                // protocol negotiation.
        listener.setInputStream(sk.getInputStream());
        listener.setServerFrameQueue(serverFrameQueue);

        listenerThread = new Thread(listener);
        listenerThread.start();

        sk.getOutputStream().write(connectFrame.toString().getBytes());
        var frame = serverFrameQueue.waitForConnected();

        if (frame instanceof StompConnectedFrame connectedFrame) {
            // set the socket timeout to twice the broker heartbeat
            // If the listener is waiting longer than this on a read, it can catch an
            // exception and detect the missed heartbeat from the broker
            int negotiatedBrokerHeartbeat = Math.max(connectFrame.brokerHeartbeat(), connectedFrame.brokerHeartbeat());
            logger.info("Setting socket timeout to 2x negotiated STOMP heartbeat of the broker: "
                    + 2 * negotiatedBrokerHeartbeat
                    + " ms");
            sk.setSoTimeout(2 * negotiatedBrokerHeartbeat);

            // set the writer heartbeat to half the client hartbeat
            // This will determine how long the writer thread will sleep if no client frames
            // are there to be sent
            logger.info("Setting heartbeat inteval for writer to half the negotiated STOMP heartbeat of the client: "
                    + negotiatedBrokerHeartbeat / 2 + " ms");
            int negotiatedClientHeartbeat = Math.max(connectFrame.clientHeartbeat(), connectedFrame.clientHeartbeat());
            writer.setHeartbeat(negotiatedClientHeartbeat / 2);
            writer.setSendBuffer(sendBuffer);
            writer.setOutputStream(sk.getOutputStream());
            writerThread = new Thread(writer);
            writerThread.start();
        }

        return frame;

    }

    @Override
    public StompServerFrameQueue getServerFrameQueue() {
        return serverFrameQueue;
    }

    @Override
    public boolean checkConnectionHealth() {
        return listener.checkConnectionHealth();
    }

    @Override
    public void close() throws Exception {
        writer.signalStop();
        listener.signalStop();
        writerThread.join();
        listenerThread.join();
    }
}
