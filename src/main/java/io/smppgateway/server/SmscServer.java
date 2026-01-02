package io.smppgateway.server;

import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Instance of SMSC server listening on one port.
 * Uses virtual threads (Java 21) for high concurrency.
 **/
public class SmscServer {
    private static final Logger logger = LoggerFactory.getLogger(SmscServer.class);

    private SmppServer smppServer;
    private final SmppServerConfiguration configuration;

    public SmscServer(SmscGlobalConfiguration config, int port, String systemId) {
        this(config, port, systemId, 10, 20, 30000);
    }

    public SmscServer(SmscGlobalConfiguration config, int port, String systemId,
                      int maxConnections, int windowSize, long requestTimeoutMs) {
        this.configuration = SmppServerConfiguration.builder()
            .port(port)
            .systemId(systemId)
            .maxConnections(maxConnections)
            .windowSize(windowSize)
            .requestTimeout(Duration.ofMillis(requestTimeoutMs))
            .build();

        this.smppServer = SmppServer.builder()
            .port(port)
            .systemId(systemId)
            .maxConnections(maxConnections)
            .windowSize(windowSize)
            .requestTimeout(Duration.ofMillis(requestTimeoutMs))
            .handler(new SmscSmppServerHandler(config))
            .build();
    }

    public void destroy() throws Exception {
        if (smppServer != null) {
            smppServer.stopSync();
            smppServer = null;
        }
    }

    public void start() {
        smppServer.startSync();
        logger.info("SMPP Server started on port {}", configuration.port());
    }

    public void stop() {
        smppServer.stopSync();
    }

    public void printMetrics() {
        logger.info("Server port={} sessions={}",
            configuration.port(),
            smppServer.getSessionCount());
    }

    public SmppServer getSmppServer() {
        return smppServer;
    }
}
