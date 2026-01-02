package io.smppgateway.controller.core;

import io.smppgateway.controller.auto.DelayedRequestSenderImpl;
import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.server.SmppServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for sending SMPP PDUs.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public class BaseSender {

    protected static final Logger logger = LoggerFactory.getLogger(BaseSender.class);

    @Autowired
    private SmppSessionManager sessionManager;

    private long sendTimeoutMillis = 5000;

    @Autowired
    private DelayedRequestSenderImpl deliverSender;

    protected void send(DeliverSm pdu) {
        SmppServerSession session = sessionManager.getNextServerSession();
        if (session != null && session.isBound()) {
            session.sendDeliverSm(pdu);
        } else {
            logger.info("Session does not exist or is not bound {}. Deliver not sent {}", session, pdu);
        }
    }

    public SmppSessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SmppSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public long getSendTimeoutMillis() {
        return sendTimeoutMillis;
    }

    public void setSendTimeoutMillis(long sendTimeoutMillis) {
        this.sendTimeoutMillis = sendTimeoutMillis;
    }

    public DelayedRequestSenderImpl getDeliverSender() {
        return deliverSender;
    }

    public void setDeliverSender(DelayedRequestSenderImpl deliverSender) {
        this.deliverSender = deliverSender;
    }
}
