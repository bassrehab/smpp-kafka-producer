package com.subhadipmitra.code.module.controller.core;

import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.subhadipmitra.code.module.controller.auto.DelayedRequestSenderImpl;
import com.subhadipmitra.code.module.controller.auto.SmppSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
public class BaseSender {

	protected static final Logger logger = LoggerFactory.getLogger(BaseSender.class);

	@Autowired
	private SmppSessionManager sessionManager;

	private long sendTimoutMilis = 5000;

	@Autowired
	private DelayedRequestSenderImpl deliverSender;

	protected void send(PduRequest pdu) throws Exception {
		SmppServerSession session = sessionManager.getNextServerSession();
		if (session != null && session.isBound()) {
			session.sendRequestPdu(pdu, 1000, false);
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

	public long getSendTimoutMilis() {
		return sendTimoutMilis;
	}

	public void setSendTimoutMilis(long sendTimoutMilis) {
		this.sendTimoutMilis = sendTimoutMilis;
	}

	public DelayedRequestSenderImpl getDeliverSender() {
		return deliverSender;
	}

	public void setDeliverSender(DelayedRequestSenderImpl deliverSender) {
		this.deliverSender = deliverSender;
	}
}
