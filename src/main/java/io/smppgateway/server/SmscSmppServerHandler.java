package io.smppgateway.server;

import io.smppgateway.controller.auto.DelayedRequestSenderImpl;
import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import io.smppgateway.controller.core.DeliveryReceiptScheduler;
import io.smppgateway.controller.core.ResponseMessageIdGenerator;
import io.smppgateway.events.service.producer.EventsProducer;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.server.SmppServerHandler;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.types.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.smppgateway.init.Main.METRICS_SMPP_PRODUCER_EVENTS_SENT;
import static io.smppgateway.init.Main.compSMPPService;
import static io.smppgateway.init.ServerMain.SESSION_PASSWORD;
import static io.smppgateway.init.ServerMain.SESSION_SYSTEMID;
import static io.smppgateway.init.ServerMain.isTestMode;

/**
 * Server handler that processes SMPP bind requests and submit_sm PDUs.
 * This merges the previous SmscSmppServerHandler and SmscSmppSessionHandler.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public class SmscSmppServerHandler implements SmppServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(SmscSmppServerHandler.class);

    private final SmppSessionManager sessionManager;
    private final SmscGlobalConfiguration config;
    private final DelayedRequestSenderImpl deliverSender;
    private final ResponseMessageIdGenerator messageIdGenerator;
    private final DeliveryReceiptScheduler deliveryReceiptScheduler;

    public SmscSmppServerHandler(SmscGlobalConfiguration config) {
        this.config = config;
        this.sessionManager = config.getSessionManager();
        this.deliverSender = config.getDeliverSender();
        this.messageIdGenerator = config.getMessageIdGenerator();
        this.deliveryReceiptScheduler = config.getDeliveryReceiptScheduler();
    }

    @Override
    public BindResult authenticate(SmppServerSession session, String systemId,
                                   String password, PduRequest<?> bindRequest) {
        if (!isTestMode) {
            // Validate SystemID
            if (!SESSION_SYSTEMID.equals(systemId)) {
                logger.info("Invalid SystemID Received: {}", systemId);
                return BindResult.failure(CommandStatus.ESME_RINVSYSID);
            }

            // Validate Password
            if (!SESSION_PASSWORD.equals(password)) {
                logger.info("Invalid Password Received: {}", password);
                return BindResult.failure(CommandStatus.ESME_RINVPASWD);
            }
        }

        logger.info("Authentication successful for systemId: {}", systemId);
        return BindResult.success(systemId);
    }

    @Override
    public SubmitSmResult handleSubmitSm(SmppServerSession session, SubmitSm submitSm) {
        try {
            long messageId = messageIdGenerator.getNextMessageId();
            String messageIdHex = FormatUtils.formatAsHex(messageId);

            if (!isTestMode) {
                // Send the SubmitSm to Queue for Kafka processing
                long eventCount = METRICS_SMPP_PRODUCER_EVENTS_SENT.incrementAndGet();
                EventsProducer prod = new EventsProducer("Producer_" + eventCount, submitSm);
                compSMPPService.submit(prod);
            }

            // Schedule delivery receipt if requested
            if (submitSm.registeredDelivery().value() > 0 && deliverSender != null) {
                DeliveryReceiptRecord record = new DeliveryReceiptRecord(
                    session, submitSm, messageId);
                record.setDeliverTime(deliveryReceiptScheduler.getDeliveryTimeMillis());
                deliverSender.scheduleDelivery(record);
            }

            return SubmitSmResult.success(messageIdHex);

        } catch (Exception e) {
            logger.error("Error handling submit_sm", e);
            return SubmitSmResult.failure(CommandStatus.ESME_RSYSERR);
        }
    }

    @Override
    public void sessionCreated(SmppServerSession session) {
        logger.info("Session created: {}", session);
    }

    @Override
    public void sessionBound(SmppServerSession session) {
        logger.info("Session bound: {} (systemId={})", session, session.getSystemId());
        sessionManager.addServerSession(session);
    }

    @Override
    public void sessionDestroyed(SmppServerSession session) {
        logger.info("Session destroyed: {}", session);
        logger.info("Final session stats - submitSm received: {}", session.getSubmitSmReceived());
        sessionManager.removeServerSession(session);
    }
}
