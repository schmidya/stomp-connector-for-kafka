package com.github.schmidya.stomp.client;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

@Testcontainers
public class StompClientTest {
        Logger logger = LoggerFactory.getLogger(StompClientTest.class);

        StompClient underTest;

        @Container
        public GenericContainer artemis = new ArtemisContainer(
                        DockerImageName.parse("docker.io/apache/activemq-artemis:latest-alpine")
                                        .asCompatibleSubstituteFor("apache/activemq-artemis"))
                        .withExposedPorts(61613);

        @BeforeEach
        public void setUp() throws Exception {
                String address = artemis.getHost();
                Integer port = artemis.getFirstMappedPort();
                String url = "tcp://" + address + ":" + port;
                underTest = StompClient.fromUrl(url);
        }

        @Test
        public void connectTest() throws Exception {
                StompServerFrame frame = underTest.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
                assertInstanceOf(StompConnectedFrame.class, frame);
        }

}
