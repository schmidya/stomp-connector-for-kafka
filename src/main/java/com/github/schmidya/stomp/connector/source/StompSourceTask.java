package com.github.schmidya.stomp.connector.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.*;

public class StompSourceTask extends SourceTask {
    
    private static final Logger log = LoggerFactory.getLogger(StompSourceTask.class);
 
    private StompClient client;
    private AbstractConfig config;
    
    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new AbstractConfig(StompSourceConnector.CONFIG_DEF, props);
        log.error("HELLO KAFKA");
        client = new StompClient(config.getString(StompSourceConnector.STOMP_BROKER_HOST_CONFIG), config.getInt(StompSourceConnector.STOMP_BROKER_PORT_CONFIG));
        log.error("CREATED CLIENT");
        try {
            log.error("CLIENT ATTEMPTING TO CONNECT");
            StompServerFrame connected_frame = client.connect("artemis", "artemis");
            log.error(connected_frame.toString());
            log.error("CLIENT ATTEMPTING SUBSCRIPTION");
            StompServerFrame sub_receit = client.subscribe(config.getString(StompSourceConnector.STOMP_DEST_CONFIG));
            log.error(sub_receit.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> ret = new ArrayList<SourceRecord>();
        List<StompMessageFrame> msgs = client.poll();
        for (StompMessageFrame msg : msgs){
            ret.add(new SourceRecord(
                Collections.singletonMap("topic", config.getString(StompSourceConnector.TOPIC_CONFIG)), 
                Collections.singletonMap("message_count", 1), 
                config.getString("topic"), 
                Schema.STRING_SCHEMA,
                msg.getBody() 
                ));
        }
        return ret;
    }

    @Override
    public void stop() {
        
    }
}
