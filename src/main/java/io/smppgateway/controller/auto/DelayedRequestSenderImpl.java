package io.smppgateway.controller.auto;

import io.smppgateway.config.initialize.ConfigurationsSourceSMPP;
import io.smppgateway.server.DelayedRecord;
import io.smppgateway.server.DelayedRequestSender;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.server.SmppServerSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.smppgateway.init.ServerMain.isTestMode;

/**
 * Class to send delayed responses.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
@Component
public class DelayedRequestSenderImpl extends DelayedRequestSender<DelayedRecord> {

    private static final Logger logger = LoggerFactory.getLogger(DelayedRequestSenderImpl.class);

    @Autowired
    private SmppSessionManager sessionManager;

    private long sendTimeoutMillis = isTestMode ? 1000 : ConfigurationsSourceSMPP.SMPP_SERVER_DELAYED_REQUEST_TIMEOUT_MS;

    public DelayedRequestSenderImpl() {
        // empty constructor
    }

    @Override
    protected void handleDelayedRecord(DelayedRecord delayedRecord) throws Exception {
        SmppServerSession session = delayedRecord.getUsedSession(sessionManager);
        PduRequest<?> request = delayedRecord.getRequest(sessionManager.getNextSequenceNumber());

        if (session != null && session.isBound()) {
            if (request instanceof DeliverSm deliverSm) {
                session.sendDeliverSm(deliverSm);
            } else {
                logger.warn("Unsupported PDU type for delayed sending: {}", request.getClass());
            }
        } else {
            logger.info("Session does not exist or is not bound {}. Request not sent {}", session, request);
        }
    }

    public SmppSessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SmppSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostConstruct
    public void init() throws Exception {
        start();
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        stop();
    }

    public long getSendTimeoutMillis() {
        return sendTimeoutMillis;
    }

    public void setSendTimeoutMillis(long sendTimeoutMillis) {
        this.sendTimeoutMillis = sendTimeoutMillis;
    }
}
