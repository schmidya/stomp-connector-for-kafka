package com.github.schmidya.stomp.connector.sink;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;
import com.github.schmidya.stomp.connector.serializer.MessageRecord;
import com.github.schmidya.stomp.connector.serializer.MessageSerializer;
import com.github.schmidya.stomp.connector.serializer.StringSerializer;

public class StompSinkTask extends SinkTask {
    private static final Logger log = LoggerFactory.getLogger(StompSinkTask.class);

    private StompClient client;
    private AbstractConfig config;
    private MessageSerializer serializer;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new AbstractConfig(StompSinkConnector.CONFIG_DEF, props);
        try {
            serializer = (MessageSerializer) config.getClass(StompSinkConnector.SERIALIZER_CLASS_CONFIG)
                    .getConstructor().newInstance();
            log.info("Create serializer instance of class: "
                    + config.getClass(StompSinkConnector.SERIALIZER_CLASS_CONFIG).toString());
        } catch (Exception e) {
            log.error("Exception during instantiation of Serializer class:" + e.getMessage());
            log.warn("Defaulting to string serializer");
            serializer = new StringSerializer();
        }
        log.trace("Attempting to create Stomp client");
        try {
            client = StompClient.fromUrl(config.getString(StompSinkConnector.STOMP_BROKER_URL_CONFIG));
            log.trace("created client");
            StompServerFrame connected_frame = client.connect(new StompConnectFrame(
                    config.getString(StompSinkConnector.STOMP_BROKER_URL_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_LOGIN_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_PASSCODE_CONFIG)));
            log.info("client successfully connected to broker:\n" + connected_frame.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            String message = serializer.serialize(new MessageRecord(record.value(), record.valueSchema()));
            client.sendMessage(message, config.getString(StompSinkConnector.STOMP_DEST_CONFIG));
        }
    }

    @Override
    public void stop() {
        try {
            client.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
