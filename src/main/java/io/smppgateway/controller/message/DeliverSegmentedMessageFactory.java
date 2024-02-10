package io.smppgateway.controller.message;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.DeliverSm;
import io.smppgateway.server.SmppPduUtils;
import io.smppgateway.controller.core.BaseSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Not thread safe, must be used by one thread only
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 **/
public class DeliverSegmentedMessageFactory extends DeliverBaseMessageFactory {

	protected static final Logger logger = LoggerFactory.getLogger(BaseSender.class);

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

		DeliverSm pdu = super.createMessage();

		String shortMessage = "Segment content " + nextSegmentId + ". ";
		SmppPduUtils.setSegmentOptionalParams(pdu, currentMsgRefNum, nextSegmentId, numberOfSegments);
		pdu.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_UCS_2));

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
