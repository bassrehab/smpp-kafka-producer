package com.subhadipmitra.code.module.controller.auto;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP;
import com.subhadipmitra.code.module.server.DelayedRecord;
import com.subhadipmitra.code.module.server.DelayedRequestSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.subhadipmitra.code.module.init.ServerMain.isTestMode;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Class to send delayed response
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
@Component
public class DelayedRequestSenderImpl extends DelayedRequestSender<DelayedRecord> {

	private static final Logger logger = LoggerFactory.getLogger(DelayedRequestSenderImpl.class);

	@Autowired
	private SmppSessionManager sessionManager;

	private long sendTimoutMilis = isTestMode? 1000 : ConfigurationsSourceSMPP.SMPP_SERVER_DELAYED_REQUEST_TIMEOUT_MS;
	public DelayedRequestSenderImpl() {
		// empty constructor
	}

	@Override
	protected void handleDelayedRecord(DelayedRecord delayedRecord) throws Exception {
		SmppSession session = delayedRecord.getUsedSession(sessionManager);
		PduRequest request = delayedRecord.getRequest(sessionManager.getNextSequenceNumber());
		if (session != null && session.isBound()) {
			session.sendRequestPdu(request, sendTimoutMilis, false);
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

	public long getSendTimoutMilis() {
		return sendTimoutMilis;
	}

	public void setSendTimoutMilis(long sendTimoutMilis) {
		this.sendTimoutMilis = sendTimoutMilis;
	}

}
