package io.smppgateway;

import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.types.SmppBindType;

import java.time.Duration;

/**
 * Test SMPP client using smpp-core.
 */
public class SmppClient {

    private final String host;
    private final int port;
    private final String systemId;

    public SmppClient(String host, int port, String systemId) {
        this.host = host;
        this.port = port;
        this.systemId = systemId;
    }

    public SmppClientSession connect(SmppClientHandler handler) throws Exception {
        io.smppgateway.smpp.client.SmppClient client = io.smppgateway.smpp.client.SmppClient.builder()
            .host(host)
            .port(port)
            .systemId(systemId)
            .password("password")
            .bindType(SmppBindType.TRANSCEIVER)
            .windowSize(10)
            .connectTimeout(Duration.ofSeconds(10))
            .requestTimeout(Duration.ofSeconds(30))
            .handler(handler)
            .build();

        return client.connect();
    }
}
