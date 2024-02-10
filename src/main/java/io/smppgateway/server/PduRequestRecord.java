package io.smppgateway.server;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import io.smppgateway.controller.auto.SmppSessionManager;
/**
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
@SuppressWarnings("rawtypes")
public class PduRequestRecord extends DelayedRecord {

	private final PduRequest request;

	public PduRequestRecord(PduRequest request, int minDelayMs, int randomDeltaMs) {
		this.request = request;
        setDeliverTime(minDelayMs, randomDeltaMs);
	}

	@Override
	public PduRequest getRequest(int sequenceNumber) throws Exception {
		request.setSequenceNumber(sequenceNumber);
		return request;
	}
	
	@Override
	public SmppSession getUsedSession(SmppSessionManager sessionManager) {
		return sessionManager.getNextServerSession();
	}

}
