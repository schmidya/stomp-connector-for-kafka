package com.github.schmidya.connector.stomp;

import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StompSourceTask extends SourceTask {

    final static String connectFrame =
        "CONNECT\n" +
        "accept-version:1.1\n" + 
        "host:localhost\n" + 
        "login:artemis\n" + 
        "passcode:artemis\n\n";
    

    final static String subFrame =
        "SUBSCRIBE\n" +
        "id:0\n" + //
        "destination:/queue/foo\n" + //
        "receipt:message-12345\n" + 
        "ack:client\n\n";
    
    private static final Logger log = LoggerFactory.getLogger(StompSourceTask.class);
 
    private Socket sk;
    private AbstractConfig config;


    private String getNextFrame() throws Exception {
        InputStream is = sk.getInputStream();
        int r = is.read();

        String out = "";
        while (r!=0){
            out += (char)r;
            r = is.read();
        }
        return out;
    }
    
    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new AbstractConfig(StompSourceConnector.CONFIG_DEF, props);
        log.error("HELLO KAFKA");
        try {
            sk = new Socket(
                config.getString(StompSourceConnector.STOMP_BROKER_HOST_CONFIG), 
                config.getInt(StompSourceConnector.STOMP_BROKER_PORT_CONFIG)
            );
            log.error(sk.isConnected() ? "Socket Connecte" : "Not connected");
            sk.getOutputStream().write(connectFrame.getBytes());
            sk.getOutputStream().write(0);
            sk.getOutputStream().flush();
            log.error(getNextFrame());
            sk.getOutputStream().write(subFrame.getBytes());
            sk.getOutputStream().write(0);
            sk.getOutputStream().flush();
            log.error(getNextFrame());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return null;
    }

    @Override
    public void stop() {
        
    }
}
