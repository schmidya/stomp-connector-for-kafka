package com.github.schmidya.stomp.client;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;
import com.github.schmidya.stomp.client.frames.StompConnectedFrame;
import com.github.schmidya.stomp.client.frames.StompServerFrame;

@Testcontainers
public class StompClientTest {

    StompClient underTest;

    @Container
    public GenericContainer artemis = new GenericContainer(
            DockerImageName.parse("docker.io/apache/activemq-artemis:latest-alpine"))
            .withExposedPorts(61613);

    @BeforeEach
    public void setUp() throws Exception {
        String address = artemis.getHost();
        Integer port = artemis.getFirstMappedPort();
        System.out.print("Hello");
        String url = "tcp://" + address + ":" + port;
        underTest = StompClient.fromUrl(url);
    }

    @Test
    public void connectTest() throws Exception {
        StompServerFrame frame = underTest.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        assertInstanceOf(StompConnectedFrame.class, frame);
    }

}
