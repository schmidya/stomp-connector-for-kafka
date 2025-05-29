package com.github.schmidya.stomp.integration;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import eu.rekawek.toxiproxy.ToxiproxyClient;

@Testcontainers
public class TcpConnectionHealthIT extends ConnectionHealthBase {
    Logger logger = LoggerFactory.getLogger(TcpConnectionHealthIT.class);

    @Rule
    public Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    @Container
    public ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.30.0-alpine")
            .withExposedPorts(61613)
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
        proxy = toxiproxyClient.createProxy("broker", "0.0.0.0:8666", "broker:61613");
        ipAddressViaToxiproxy = toxiproxy.getHost();
        portViaToxiproxy = toxiproxy.getMappedPort(8666);
        brokerProxyUrl = "tcp://" + ipAddressViaToxiproxy + ":" + portViaToxiproxy;
    }

}
