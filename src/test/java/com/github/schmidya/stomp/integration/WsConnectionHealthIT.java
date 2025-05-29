package com.github.schmidya.stomp.integration;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import eu.rekawek.toxiproxy.ToxiproxyClient;

@Testcontainers
public class WsConnectionHealthIT extends ConnectionHealthBase {
    Logger logger = LoggerFactory.getLogger(WsConnectionHealthIT.class);

    @Rule
    public Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    @Container
    public ArtemisContainer artemis = new ArtemisContainer(
            DockerImageName.parse("docker.io/apache/activemq-artemis:latest-alpine")
                    .asCompatibleSubstituteFor("apache/activemq-artemis"))
            .withClasspathResourceMapping("integration/stomp-broker.xml",
                    "/var/lib/artemis-instance/etc-override/broker.xml",
                    BindMode.READ_ONLY)
            .withExposedPorts(61614)
            .withNetwork(network)
            .withNetworkAliases("broker")
            .waitingFor(Wait.forListeningPort());

    @SuppressWarnings("resource")
    @Container
    public ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.12.0")
            .withExposedPorts(8474, 8666)
            .withNetwork(network);

    @BeforeEach
    public void setUp() throws Exception {
        artemis.start();
        toxiproxy.start();
        toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        proxy = toxiproxyClient.createProxy("broker", "0.0.0.0:8666", "broker:61614");
        ipAddressViaToxiproxy = toxiproxy.getHost();
        portViaToxiproxy = toxiproxy.getMappedPort(8666);
        brokerProxyUrl = "ws://" + ipAddressViaToxiproxy + ":" + portViaToxiproxy + "/stomp";
    }

}
