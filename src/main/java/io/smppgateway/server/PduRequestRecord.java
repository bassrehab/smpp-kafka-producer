package io.smppgateway.server;

import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.server.SmppServerSession;

/**
 * Record for storing a pre-built PDU request for delayed sending.
 * Note: Since PDUs are now immutable, the sequence number must be set at creation time.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public class PduRequestRecord extends DelayedRecord {

    private final DeliverSm request;

    public PduRequestRecord(DeliverSm request, int minDelayMs, int randomDeltaMs) {
        this.request = request;
        setDeliverTime(minDelayMs, randomDeltaMs);
    }

    @Override
    public PduRequest<?> getRequest(int sequenceNumber) throws Exception {
        // PDUs are immutable - we return the pre-built request
        // Note: In a full implementation, you might want to rebuild with new sequence number
        return request;
    }

    @Override
    public SmppServerSession getUsedSession(SmppSessionManager sessionManager) {
        return sessionManager.getNextServerSession();
    }
}
