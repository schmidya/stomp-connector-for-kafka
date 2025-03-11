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
import org.apache.kafka.connect.source.ExactlyOnceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StompSinkConnector extends SinkConnector {
    public static final String STOMP_BROKER_HOST_CONFIG = "stomp.broker.host";
    public static final String STOMP_BROKER_PORT_CONFIG = "stomp.broker.port";
    public static final String STOMP_DEST_CONFIG = "stomp.destination";
    public static final String TOPIC_CONFIG = "topics";

    private static final Logger log = LoggerFactory.getLogger(StompSinkConnector.class);
    
    static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(STOMP_BROKER_HOST_CONFIG, Type.STRING, "localhost", Importance.HIGH, "Hostname/IP of broker")
        .define(STOMP_BROKER_PORT_CONFIG, Type.INT, 61613, Importance.HIGH, "Port of STOMP BROKER")
        .define(STOMP_DEST_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, Importance.HIGH, "STOMP destination to subscribe to")
        .define(TOPIC_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, new ConfigDef.NonEmptyString(), Importance.HIGH, "The topic to publish data to");

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
    //     return ExactlyOnceSupport.UNSUPPORTED;
    // }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
    
}
