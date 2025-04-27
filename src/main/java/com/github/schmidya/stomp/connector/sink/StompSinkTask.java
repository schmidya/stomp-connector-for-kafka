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

public class StompSinkTask extends SinkTask {
    private static final Logger log = LoggerFactory.getLogger(StompSinkTask.class);

    private StompClient client;
    private AbstractConfig config;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new AbstractConfig(StompSinkConnector.CONFIG_DEF, props);
        log.error("HELLO KAFKA");
        try {
            client = StompClient.fromUrl(config.getString(StompSinkConnector.STOMP_BROKER_URL_CONFIG));
            log.error("CREATED CLIENT");
            log.error("CLIENT ATTEMPTING TO CONNECT");
            StompServerFrame connected_frame = client.connect(new StompConnectFrame(
                    config.getString(StompSinkConnector.STOMP_BROKER_URL_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_LOGIN_CONFIG),
                    config.getString(StompSinkConnector.STOMP_BROKER_PASSCODE_CONFIG)));
            log.error(connected_frame.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            client.sendMessage(record.value().toString(), config.getString(StompSinkConnector.STOMP_DEST_CONFIG));

        }
    }

    @Override
    public void stop() {

    }

}
