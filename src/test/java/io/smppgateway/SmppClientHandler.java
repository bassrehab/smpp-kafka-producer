package io.smppgateway;

import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.pdu.DeliverSm;

/**
 * Simple client handler interface for testing.
 */
public interface SmppClientHandler extends io.smppgateway.smpp.client.SmppClientHandler {

    @Override
    default DeliverSmResult handleDeliverSm(SmppClientSession session, DeliverSm deliverSm) {
        return DeliverSmResult.success();
    }
}
