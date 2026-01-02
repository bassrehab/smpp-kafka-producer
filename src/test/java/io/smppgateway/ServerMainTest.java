package io.smppgateway;

import com.cloudhopper.commons.charset.CharsetUtil;
import io.smppgateway.server.SmscServer;
import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.pdu.SubmitSmResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.DataCoding;
import io.smppgateway.smpp.types.RegisteredDelivery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Server integration tests.
 *
 * NOTE: These tests require additional Spring context setup for full integration testing.
 * The basic SimpleServerTest verifies smpp-core bind handling works correctly.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("/context.xml")
@Disabled("Full integration test - requires complete Spring context with Kafka")
public class ServerMainTest {

    private static final Logger logger = LoggerFactory.getLogger(ServerMainTest.class);

    private static final int PORT = 12345;
    private static final String SYSTEM_ID = "132456";
    private static final String SYSTEM_ID_2 = "789798";

    private static final int NUMBER_OF_SUBMITS = 20;
    private static final int NUMBER_OF_SUBMITS_2 = 50;

    @Autowired
    private ApplicationContext context;

    private SmscServer smscServer;

    @BeforeEach
    public void before() throws Exception {
        SmscGlobalConfiguration smscConfiguration = context.getBean(SmscGlobalConfiguration.class);

        // Create server using smpp-core fluent builder
        smscServer = new SmscServer(
            smscConfiguration,
            PORT,
            "testsmpp",
            10,
            20,
            30000L
        );
        smscServer.start();
    }

    @AfterEach
    public void after() {
        try {
            smscServer.stop();
            smscServer.destroy();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connect 1 session
     * Send number of submit sm messages
     * Check that correct delivery receipt count is delivered to back to session.
     **/
    @Test
    public void testSubmitsAndDeliveryReceipts() throws Exception {
        SmppClient client = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingSmppClientHandler handler = new BlockingSmppClientHandler();
        SmppClientSession session = client.connect(handler);

        for (int i = 0; i < NUMBER_OF_SUBMITS; i++) {
            SubmitSm submitSm = createSubmitWithRegisteredDelivery();
            SubmitSmResp resp = session.submitSm(submitSm, Duration.ofSeconds(10));
            if (resp.commandStatus() == CommandStatus.ESME_ROK) {
                handler.responseReceived();
            }
        }

        handler.blockUntilReceived(NUMBER_OF_SUBMITS, NUMBER_OF_SUBMITS);

        session.close();
    }

    /**
     * Connect 2 sessions
     * Send different message count from each one
     * Check that correct delivery receipt count is delivered to each session.
     **/
    @Test
    public void testSubmitsAndDeliveryReceipts2() throws Exception {
        SmppClient client1 = new SmppClient("localhost", PORT, SYSTEM_ID);
        BlockingSmppClientHandler handler1 = new BlockingSmppClientHandler();
        SmppClientSession session1 = client1.connect(handler1);

        for (int i = 0; i < NUMBER_OF_SUBMITS; i++) {
            SubmitSm submitSm = createSubmitWithRegisteredDelivery();
            SubmitSmResp resp = session1.submitSm(submitSm, Duration.ofSeconds(10));
            if (resp.commandStatus() == CommandStatus.ESME_ROK) {
                handler1.responseReceived();
            }
        }

        SmppClient client2 = new SmppClient("localhost", PORT, SYSTEM_ID_2);
        BlockingSmppClientHandler handler2 = new BlockingSmppClientHandler();
        SmppClientSession session2 = client2.connect(handler2);

        for (int i = 0; i < NUMBER_OF_SUBMITS_2; i++) {
            SubmitSm submitSm = createSubmitWithRegisteredDelivery();
            SubmitSmResp resp = session2.submitSm(submitSm, Duration.ofSeconds(10));
            if (resp.commandStatus() == CommandStatus.ESME_ROK) {
                handler2.responseReceived();
            }
        }

        handler1.blockUntilReceived(NUMBER_OF_SUBMITS, NUMBER_OF_SUBMITS);
        handler2.blockUntilReceived(NUMBER_OF_SUBMITS_2, NUMBER_OF_SUBMITS_2);

        session1.close();
        session2.close();
    }

    private SubmitSm createSubmitWithRegisteredDelivery() {
        Address sourceAddress = new Address((byte) 0, (byte) 0, "987654321");
        Address destAddress = new Address((byte) 0, (byte) 0, "123456789");

        String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
        byte[] messageBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);

        return SubmitSm.builder()
            .sourceAddress(sourceAddress)
            .destAddress(destAddress)
            .shortMessage(messageBytes)
            .dataCoding(DataCoding.DEFAULT)
            .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
            .build();
    }

    /**
     * Simple client handler which enables waiting on specific response / deliver sm count by blocking on semaphore.
     **/
    public static class BlockingSmppClientHandler implements SmppClientHandler {

        private final Semaphore responseSem = new Semaphore(0);
        private final Semaphore deliverSem = new Semaphore(0);
        private final AtomicInteger deliverCount = new AtomicInteger(0);

        @Override
        public io.smppgateway.smpp.client.SmppClientHandler.DeliverSmResult handleDeliverSm(
                SmppClientSession session, DeliverSm deliverSm) {
            logger.info("DeliverSm received: {}", deliverSm);
            deliverCount.incrementAndGet();
            deliverSem.release();
            return DeliverSmResult.success();
        }

        @Override
        public void sessionBound(SmppClientSession session) {
            logger.info("Session bound: {}", session);
        }

        @Override
        public void sessionUnbound(SmppClientSession session) {
            logger.info("Session unbound: {}", session);
        }

        public void responseReceived() {
            responseSem.release();
        }

        public void blockUntilReceived(int expectedResponses, int expectedDeliverSm) throws InterruptedException {
            logger.info("Waiting for {} responses", expectedResponses);
            responseSem.acquire(expectedResponses);
            logger.info("All responses received, waiting for {} delivers", expectedDeliverSm);
            deliverSem.acquire(expectedDeliverSm);
            logger.info("All delivers received (count={})", deliverCount.get());
        }
    }
}
