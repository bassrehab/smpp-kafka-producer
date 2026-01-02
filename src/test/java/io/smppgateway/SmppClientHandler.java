package io.smppgateway;

import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.pdu.DeliverSmResp;
import io.smppgateway.smpp.types.CommandStatus;

/**
 * Simple client handler interface for testing.
 */
public interface SmppClientHandler extends io.smppgateway.smpp.client.SmppClientHandler {

    @Override
    default DeliverSmResp handleDeliverSm(SmppClientSession session, DeliverSm deliverSm) {
        return DeliverSmResp.success(deliverSm.sequenceNumber());
    }

    @Override
    default void sessionCreated(SmppClientSession session) {
        // Default no-op
    }

    @Override
    default void sessionClosed(SmppClientSession session) {
        // Default no-op
    }
}
