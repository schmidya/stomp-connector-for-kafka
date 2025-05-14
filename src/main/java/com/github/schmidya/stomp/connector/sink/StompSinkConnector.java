package com.github.schmidya.stomp.connector.sink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schmidya.stomp.connector.serializer.StringSerializer;

public class StompSinkConnector extends SinkConnector {
    public static final String STOMP_BROKER_URL_CONFIG = "stomp.broker.url";
    public static final String STOMP_BROKER_LOGIN_CONFIG = "stomp.broker.login";
    public static final String STOMP_BROKER_PASSCODE_CONFIG = "stomp.broker.passcode";
    public static final String STOMP_DEST_CONFIG = "stomp.destination";
    public static final String TOPIC_CONFIG = "topics";
    public static final String SERIALIZER_CLASS_CONFIG = "stomp.messaging.serializer";

    private static final Logger log = LoggerFactory.getLogger(StompSinkConnector.class);

    static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(STOMP_BROKER_URL_CONFIG, Type.STRING, "localhost", Importance.HIGH, "broker url")
            .define(STOMP_BROKER_LOGIN_CONFIG, Type.STRING, null, Importance.HIGH,
                    "login for broker (e.g. artemis)")
            .define(STOMP_BROKER_PASSCODE_CONFIG, Type.STRING, null, Importance.HIGH,
                    "passcode for broker (e.g. artemis")
            .define(STOMP_DEST_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, Importance.HIGH,
                    "STOMP destination to subscribe to")
            .define(TOPIC_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, new ConfigDef.NonEmptyString(),
                    Importance.HIGH, "The topic to publish data to")
            .define(SERIALIZER_CLASS_CONFIG, Type.CLASS, StringSerializer.class, Importance.MEDIUM,
                    "The class that will (de)serialize Stomp messages");

    private Map<String, String> props;

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        this.props = props;
        AbstractConfig config = new AbstractConfig(CONFIG_DEF, props);
        log.info("Starting STOMP sink connector reading from {}", config.getString(STOMP_DEST_CONFIG));
    }

    @Override
    public Class<? extends Task> taskClass() {
        return StompSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input stream makes sense.
        configs.add(props);
        return configs;
    }

    @Override
    public void stop() {

    }

    // @Override
    // public ExactlyOnceSupport exactlyOnceSupport(Map<String, String> props) {
    // return ExactlyOnceSupport.UNSUPPORTED;
    // }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

}
