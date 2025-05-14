package com.github.schmidya.stomp.connector.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.*;
import com.github.schmidya.stomp.connector.serializer.MessageRecord;
import com.github.schmidya.stomp.connector.serializer.MessageSerializer;
import com.github.schmidya.stomp.connector.serializer.StringSerializer;
import com.github.schmidya.stomp.connector.sink.StompSinkConnector;

public class StompSourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(StompSourceTask.class);

    private StompClient client;
    private AbstractConfig config;
    private MessageSerializer serializer;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new AbstractConfig(StompSourceConnector.CONFIG_DEF, props);
        try {
            serializer = (MessageSerializer) config.getClass(StompSourceConnector.SERIALIZER_CLASS_CONFIG)
                    .getConstructor().newInstance();
            log.error("HELLO KAFKA");
        } catch (Exception e) {
            log.error("Exception during instantiation of Serializer class:" + e.getMessage());
            log.warn("Defaulting to string serializer");
            serializer = new StringSerializer();
        }
        try {
            client = StompClient.fromUrl(config.getString(StompSourceConnector.STOMP_BROKER_URL_CONFIG));
            log.error("CREATED CLIENT");
            log.error("CLIENT ATTEMPTING TO CONNECT");
            StompServerFrame connected_frame = client.connect(new StompConnectFrame(
                    config.getString(StompSinkConnector.STOMP_BROKER_URL_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_LOGIN_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_PASSCODE_CONFIG)));
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
        for (StompMessageFrame msg : msgs) {
            MessageRecord r = serializer.deserialize(msg.getBody());
            ret.add(new SourceRecord(
                    Collections.singletonMap("topic", config.getString(StompSourceConnector.TOPIC_CONFIG)),
                    Collections.singletonMap("message_count", 1),
                    config.getString(StompSourceConnector.TOPIC_CONFIG),
                    r.schema(),
                    r.value()));
        }
        return ret;
    }

    @Override
    public void stop() {

    }
}
