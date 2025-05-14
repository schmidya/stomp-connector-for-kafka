package com.github.schmidya.stomp.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

@Testcontainers
public class ArtemisTcpIT {
    Logger logger = LoggerFactory.getLogger(ArtemisTcpIT.class);

    @SuppressWarnings("resource")
    @Container
    public ComposeContainer environment = new ComposeContainer(
            new File("src/test/resources/integration/ArtemisTcpIT.yaml"))
            .withEnv("CONNECTOR_JAR_PATH", System.getProperty("connector.jar.path"))
            .withExposedService("broker-1", 61613)
            .withExposedService("kafka-connect-1", 8083)
            .waitingFor("kafka-connect-1",
                    Wait.forHttp("/connectors/stomp-source/status").forPort(8083)
                            .forResponsePredicate(s -> s.contains("RUNNING")))
            .waitingFor("kafka-connect-1",
                    Wait.forHttp("/connectors/stomp-sink/status").forPort(8083)
                            .forResponsePredicate(s -> s.contains("RUNNING")))
            .withLocalCompose(true);

    @Test
    public void test() throws Exception {
        StompClient client = StompClient.fromUrl("tcp://" + environment.getServiceHost("broker-1", 61613) + ":"
                + environment.getServicePort("broker-1", 61613));
        StompServerFrame frame = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));

        assertInstanceOf(StompConnectedFrame.class, frame);

        client.subscribe("/dest/sink");

        client.sendMessage("{\"hello\" : \"world\"}", "/dest/source");

        Thread.sleep(10000);

        logger.info(environment.getContainerByServiceName("kafka-debug-consumer-1").get().getLogs());
        logger.info(environment.getContainerByServiceName("kafka-connect-1").get().getLogs());

        var messages = client.poll();

        assertEquals(1, messages.size());
    }

}
