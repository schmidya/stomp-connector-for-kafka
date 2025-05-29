package com.github.schmidya.stomp.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompReceiptFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

@Testcontainers
public class WsStompClientIT {
    Logger logger = LoggerFactory.getLogger(WsStompClientIT.class);

    StompClient underTest;

    @SuppressWarnings("resource")
    @Container
    public ArtemisContainer artemis = new ArtemisContainer(
            DockerImageName.parse("docker.io/apache/activemq-artemis:latest-alpine")
                    .asCompatibleSubstituteFor("apache/activemq-artemis"))
            .withExposedPorts(61614)
            .withClasspathResourceMapping("integration/stomp-broker.xml",
                    "/var/lib/artemis-instance/etc-override/broker.xml",
                    BindMode.READ_ONLY)
            .waitingFor(Wait.forListeningPort());

    @BeforeEach
    public void setUp() throws Exception {
        String address = artemis.getHost();
        Integer port = artemis.getFirstMappedPort();
        String url = "ws://" + address + ":" + port + "/stomp";
        underTest = StompClient.fromUrl(url);
    }

    @AfterEach
    public void tearDown() throws Exception {
        underTest.close();
    }

    @Test
    public void connectTest() throws Exception {
        StompServerFrame frame = underTest.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        logger.info(frame.toString());
        assertInstanceOf(StompConnectedFrame.class, frame);
    }

    @Test
    public void echoTest() throws Exception {
        StompServerFrame frame = underTest.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        assertInstanceOf(StompConnectedFrame.class, frame);

        StompReceiptFrame f = underTest.subscribe("dest/echo");
        logger.info(f.toString());

        underTest.sendMessage("{'hello' : 'world'}", "dest/echo");

        Thread.sleep(500);

        var messages = underTest.poll();

        assertEquals(messages.size(), 1);
        logger.info(messages.get(0).toString());
        assertEquals("{'hello' : 'world'}", messages.get(0).getBody());

    }

}
