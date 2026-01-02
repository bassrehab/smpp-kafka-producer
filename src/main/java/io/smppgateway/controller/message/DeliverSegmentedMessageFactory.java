package io.smppgateway.controller.message;

import com.cloudhopper.commons.charset.CharsetUtil;
import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.controller.core.BaseSender;
import io.smppgateway.server.SmppPduUtils;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.types.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for creating segmented DeliverSm PDUs.
 * Not thread safe, must be used by one thread only.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 **/
public class DeliverSegmentedMessageFactory extends DeliverBaseMessageFactory {

    protected static final Logger logger = LoggerFactory.getLogger(BaseSender.class);

    @Autowired
    private SmppSessionManager sessionManager;

    private int nextMsgRefNum = 1;
    private int numberOfSegments = 10;
    private int nextSegmentId = 1;
    private int currentMsgRefNum = 1;

    @Override
    public DeliverSm createMessage() throws Exception {
        if (nextSegmentId > numberOfSegments) {
            // we are starting a new segmented message
            currentMsgRefNum = ++nextMsgRefNum;
            nextSegmentId = 1;
        }

        Address sourceAddress = new Address(
            (byte) getSourceAddressTon(),
            (byte) getSourceAddressNpi(),
            getSourceAddressDigits());
        Address destinationAddress = new Address(
            (byte) getDestAddressTon(),
            (byte) getDestAddressNpi(),
            getDestAddressDigits());

        String shortMessage = "Segment content " + nextSegmentId + ". ";
        byte[] messageBytes = CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_UCS_2);

        DeliverSm pdu = SmppPduUtils.createDeliverSmWithSarTlv(
            sourceAddress,
            destinationAddress,
            sessionManager.getNextSequenceNumber(),
            currentMsgRefNum,
            nextSegmentId,
            numberOfSegments,
            messageBytes);

        nextSegmentId += 1;
        return pdu;
    }

    public int getNumberOfSegments() {
        return numberOfSegments;
    }

    public void setNumberOfSegments(int numberOfSegments) {
        this.numberOfSegments = numberOfSegments;
    }
}
