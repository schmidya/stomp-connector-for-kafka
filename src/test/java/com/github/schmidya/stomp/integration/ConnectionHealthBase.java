package com.github.schmidya.stomp.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.slf4j.Logger;

import org.junit.jupiter.api.Test;
import com.github.schmidya.stomp.client.StompClient;
import com.github.schmidya.stomp.client.frames.StompConnectFrame;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;

public class ConnectionHealthBase {

    protected Logger logger;

    protected ToxiproxyClient toxiproxyClient;
    protected Proxy proxy;
    protected String ipAddressViaToxiproxy;
    protected int portViaToxiproxy;
    protected String brokerProxyUrl;

    @Test
    public void healthyConnection() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        Thread.sleep(100);

        assertTrue(client.checkConnectionHealth());
    }

    @Test
    public void healthyConnectionSilent() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        Thread.sleep(4000);

        assertTrue(client.checkConnectionHealth());
    }

    @Test
    public void cutBidirectional() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("UPSTREAM", ToxicDirection.UPSTREAM, 1);
        proxy.toxics().timeout("DOWNSTREAM", ToxicDirection.DOWNSTREAM, 1);

        Thread.sleep(100);

        assertFalse(client.checkConnectionHealth());

        proxy.toxics().get("UPSTREAM").remove();
        proxy.toxics().get("DOWNSTREAM").remove();

    }

    @Test
    public void cutDownstream() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("DOWNSTREAM", ToxicDirection.DOWNSTREAM, 1);

        Thread.sleep(100);

        assertFalse(client.checkConnectionHealth());
        proxy.toxics().get("DOWNSTREAM").remove();

    }

    @Test
    public void cutUpstream() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("UPSTREAM", ToxicDirection.UPSTREAM, 1);

        Thread.sleep(100);

        assertFalse(client.checkConnectionHealth());
        proxy.toxics().get("UPSTREAM").remove();

    }

    @Test
    public void cutBidirectionalSilent() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("UPSTREAM", ToxicDirection.UPSTREAM, 0);
        proxy.toxics().timeout("DOWNSTREAM", ToxicDirection.DOWNSTREAM, 0);

        Thread.sleep(5000);

        assertFalse(client.checkConnectionHealth());

        proxy.toxics().get("UPSTREAM").remove();
        proxy.toxics().get("DOWNSTREAM").remove();

    }

    @Test
    public void cutDownstreamSilent() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("DOWNSTREAM", ToxicDirection.DOWNSTREAM, 0);

        Thread.sleep(5000);

        assertFalse(client.checkConnectionHealth());
        proxy.toxics().get("DOWNSTREAM").remove();

    }

    @Test
    public void cutUpstreamSilent() throws Exception {

        StompClient client = StompClient.fromUrl(brokerProxyUrl);
        var f = client.connect(new StompConnectFrame("artemis", "artemis", "artemis"));
        client.subscribe("test");

        proxy.toxics().timeout("UPSTREAM", ToxicDirection.UPSTREAM, 0);

        Thread.sleep(5000);

        assertFalse(client.checkConnectionHealth());
        proxy.toxics().get("UPSTREAM").remove();

    }
}
