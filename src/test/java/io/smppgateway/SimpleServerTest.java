package io.smppgateway;

import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerHandler;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify smpp-core server/client work together.
 */
public class SimpleServerTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServerTest.class);

    @Test
    public void testServerStartsAndAcceptsConnection() throws Exception {
        logger.info("Starting test server on port 12346...");

        // Create a minimal server
        SmppServer server = SmppServer.builder()
            .port(12346)
            .systemId("testsmsc")
            .maxConnections(10)
            .windowSize(10)
            .requestTimeout(Duration.ofSeconds(30))
            .handler(new TestHandler())
            .build();

        try {
            server.startSync();
            logger.info("Server started successfully");
            assertTrue(server.isRunning(), "Server should be running");
            assertEquals(12346, server.getPort(), "Server port should be 12346");

            // Create client and connect
            logger.info("Connecting client...");
            io.smppgateway.smpp.client.SmppClient client = io.smppgateway.smpp.client.SmppClient.builder()
                .host("localhost")
                .port(12346)
                .systemId("testclient")
                .password("password")
                .bindType(io.smppgateway.smpp.types.SmppBindType.TRANSCEIVER)
                .windowSize(10)
                .connectTimeout(Duration.ofSeconds(5))
                .bindTimeout(Duration.ofSeconds(10))
                .requestTimeout(Duration.ofSeconds(10))
                .handler(new TestClientHandler())
                .build();

            io.smppgateway.smpp.client.SmppClientSession session = client.connect();
            logger.info("Client connected and bound successfully");
            assertTrue(session.isBound(), "Session should be bound");

            // Disconnect
            session.unbind();
            session.close();
            logger.info("Client disconnected");

        } finally {
            server.stopSync();
            logger.info("Server stopped");
        }
    }

    private static class TestHandler implements SmppServerHandler {
        @Override
        public BindResult authenticate(SmppServerSession session, String systemId,
                                        String password, PduRequest<?> bindRequest) {
            logger.info("Authenticating: systemId={}", systemId);
            return BindResult.success(systemId);
        }

        @Override
        public SubmitSmResult handleSubmitSm(SmppServerSession session, SubmitSm submitSm) {
            logger.info("Received submit_sm");
            return SubmitSmResult.success("12345");
        }

        @Override
        public void sessionCreated(SmppServerSession session) {
            logger.info("Session created: {}", session.getSessionId());
        }

        @Override
        public void sessionBound(SmppServerSession session) {
            logger.info("Session bound: {}", session.getSessionId());
        }

        @Override
        public void sessionDestroyed(SmppServerSession session) {
            logger.info("Session destroyed: {}", session.getSessionId());
        }
    }

    private static class TestClientHandler implements io.smppgateway.smpp.client.SmppClientHandler {
        @Override
        public DeliverSmResult handleDeliverSm(io.smppgateway.smpp.client.SmppClientSession session,
                                                io.smppgateway.smpp.pdu.DeliverSm deliverSm) {
            logger.info("Received deliver_sm");
            return DeliverSmResult.success();
        }
    }
}
