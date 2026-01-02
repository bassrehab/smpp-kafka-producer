package io.smppgateway.server;

import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.types.Address;

import java.time.LocalDateTime;

/**
 * Record for tracking delivery receipts to be sent.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public class DeliveryReceiptRecord extends DelayedRecord {

    private final Address sourceAddress;
    private final Address destinationAddress;
    private final long messageId;
    private final LocalDateTime submitDate;

    public DeliveryReceiptRecord(SmppServerSession session, SubmitSm pduRequest, long messageId) {
        super(session);
        // Swap source and destination for delivery receipt
        this.sourceAddress = pduRequest.destAddress();
        this.destinationAddress = pduRequest.sourceAddress();
        this.messageId = messageId;
        this.submitDate = LocalDateTime.now();
    }

    public Address getSourceAddress() {
        return sourceAddress;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public long getMessageId() {
        return messageId;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    @Override
    public SmppServerSession getUsedSession(SmppSessionManager sessionManager) {
        String systemId = getSession().getSystemId();
        return sessionManager.getNextServerSession(systemId);
    }

    @Override
    public PduRequest<?> getRequest(int sequenceNumber) throws Exception {
        return SmppPduUtils.createDeliveryReceipt(this, sequenceNumber);
    }
}
