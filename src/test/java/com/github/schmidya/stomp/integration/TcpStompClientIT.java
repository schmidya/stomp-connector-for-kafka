package com.github.schmidya.stomp.integration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompReceiptFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

@Testcontainers
public class TcpStompClientIT {
        Logger logger = LoggerFactory.getLogger(TcpStompClientIT.class);

        StompClient underTest;

        @SuppressWarnings("resource")
        @Container
        public ArtemisContainer artemis = new ArtemisContainer(
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

                assertEquals("{'hello' : 'world'}", messages.get(0).getBody());

        }

}
