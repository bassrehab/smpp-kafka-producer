package com.subhadipmitra.code.module.controller.auto;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.type.Address;
import com.subhadipmitra.code.module.controller.core.BaseSender;
import com.subhadipmitra.code.module.server.PduRequestRecord;
import com.subhadipmitra.code.module.server.SmppPduUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 *
 */
@Component
@ManagedResource(objectName = "smppserver:name=SmscSegmentedDeliverSender")
public class SmscSegmentedDeliverSender extends BaseSender {

	private AtomicInteger nextMsgRefNum = new AtomicInteger(1);

	@ManagedOperation
	public void sendSegmentedDeliverMsg(int numberOfSegments) throws Exception {
		Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
		Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

		int msgRefNum = nextMsgRefNum.incrementAndGet();
		for (int i = 1; i <= numberOfSegments; i++) {
			String shortMessage = "Segment content " + i + ". ";

			DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
			SmppPduUtils.setSegmentOptionalParams(pdu, msgRefNum, i, numberOfSegments);
			pdu.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_UCS_2));
			send(pdu);
		}
	}

	@ManagedOperation
	public void sendSegmentedDeliverLongMsg(int numberOfSegments, int minMsgSize) throws Exception {
		Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
		Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

		int msgRefNum = nextMsgRefNum.incrementAndGet();
		for (int i = 1; i <= numberOfSegments; i++) {
			StringBuilder sb = new StringBuilder(255);
			sb.append("Segment content ");
			sb.append(i);
			sb.append(". ");
			while (sb.length() < minMsgSize) {
				sb.append(".");
			}
			String shortMessage = sb.toString();

			DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
			SmppPduUtils.setSegmentOptionalParams(pdu, msgRefNum, i, numberOfSegments);

			byte[] body = CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_UCS_2);
			if (body.length < 256) {
				pdu.setShortMessage(body);
			} else {
				SmppPduUtils.setMessagePayloadOptionalParams(pdu, body);
			}
			pdu.setDataCoding(SmppConstants.DATA_CODING_8BITA);

			send(pdu);
		}
	}

	@ManagedOperation
	public void sendSegmentedBinaryDeliverMsg(int numberOfSegments) throws Exception {
		Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
		Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

		int msgRefNum = nextMsgRefNum.incrementAndGet();
		for (int i = 1; i <= numberOfSegments; i++) {
			String shortMessage = "Segment content " + i + ". ";

			DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
			SmppPduUtils.setSegmentOptionalParams(pdu, msgRefNum, i, numberOfSegments);
			pdu.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.NAME_UCS_2));
			pdu.setDataCoding((byte) 0x02); // 8 bit binary encoding
			send(pdu);
		}
	}

	@ManagedOperation
	public void sendSegmentedDeliverMsgUdh00(int numberOfSegments) throws Exception {
		Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
		Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

		int msgRefNum = nextMsgRefNum.incrementAndGet();
		for (int i = 1; i <= numberOfSegments; i++) {
			String shortMessage = "Segment content " + i + ". ";

			DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
			SmppPduUtils.setSegmentUdh00AndMessage(pdu, msgRefNum, i, numberOfSegments, shortMessage);
			send(pdu);
		}
	}

	@ManagedOperation
	public void sendSegmentedDeliverMsgUdh08(int numberOfSegments) throws Exception {
		Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
		Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

		int msgRefNum = nextMsgRefNum.incrementAndGet();
		for (int i = 1; i <= numberOfSegments; i++) {
			String shortMessage = "Segment content " + i + ". ";

			DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
			SmppPduUtils.setSegmentUdh08AndMessage(pdu, msgRefNum, i, numberOfSegments, shortMessage);
			send(pdu);
		}
	}

	@ManagedOperation
	public void sendSegmentedDeliverMsgRandomized(int numberOfMessages, int numberOfSegments) throws Exception {
		for (int msgNum = 0; msgNum < numberOfMessages; msgNum++) {
			Address sourceAddress = new Address((byte) 0, (byte) 0, "123456789");
			Address destinationAddress = new Address((byte) 0, (byte) 0, "987654321");

			int msgRefNum = nextMsgRefNum.incrementAndGet();
			for (int i = 1; i <= numberOfSegments; i++) {
				String shortMessage = "Msg " + msgNum + ". Segment content " + i + ". ";
				DeliverSm pdu = SmppPduUtils.createDeliverSm(sourceAddress, destinationAddress, getSessionManager().getNextSequenceNumber());
				SmppPduUtils.setSegmentOptionalParams(pdu, msgRefNum, i, numberOfSegments);
				pdu.setShortMessage(CharsetUtil.encode(shortMessage, CharsetUtil.CHARSET_UCS_2));
				getDeliverSender().scheduleDelivery(new PduRequestRecord(pdu, 1000, 5000));
			}
		}
	}

}
