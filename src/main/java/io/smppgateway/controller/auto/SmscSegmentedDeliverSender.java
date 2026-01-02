package io.smppgateway.controller.auto;

import io.smppgateway.smpp.charset.SmppCharset;
import io.smppgateway.controller.core.BaseSender;
import io.smppgateway.server.PduRequestRecord;
import io.smppgateway.server.SmppPduUtils;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.DataCoding;
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
			byte[] messageBytes = SmppCharset.encodeUcs2(shortMessage);

			DeliverSm pdu = SmppPduUtils.createDeliverSmWithSarTlv(
				sourceAddress,
				destinationAddress,
				getSessionManager().getNextSequenceNumber(),
				msgRefNum,
				i,
				numberOfSegments,
				messageBytes
			);
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

			byte[] body = SmppCharset.encodeUcs2(shortMessage);
			DeliverSm pdu;

			if (body.length < 256) {
				pdu = SmppPduUtils.createDeliverSmWithSarTlv(
					sourceAddress,
					destinationAddress,
					getSessionManager().getNextSequenceNumber(),
					msgRefNum,
					i,
					numberOfSegments,
					body
				);
			} else {
				// For long messages, use message_payload TLV
				pdu = SmppPduUtils.createDeliverSmWithMessagePayload(
					sourceAddress,
					destinationAddress,
					getSessionManager().getNextSequenceNumber(),
					msgRefNum,
					i,
					numberOfSegments,
					body,
					DataCoding.OCTET_UNSPECIFIED
				);
			}
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
			byte[] messageBytes = SmppCharset.encodeUcs2(shortMessage);

			DeliverSm pdu = SmppPduUtils.createDeliverSmWithSarTlvAndDataCoding(
				sourceAddress,
				destinationAddress,
				getSessionManager().getNextSequenceNumber(),
				msgRefNum,
				i,
				numberOfSegments,
				messageBytes,
				DataCoding.OCTET_UNSPECIFIED
			);
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

			DeliverSm pdu = SmppPduUtils.createDeliverSmWithUdh00(
				sourceAddress,
				destinationAddress,
				getSessionManager().getNextSequenceNumber(),
				msgRefNum,
				i,
				numberOfSegments,
				shortMessage
			);
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

			DeliverSm pdu = SmppPduUtils.createDeliverSmWithUdh08(
				sourceAddress,
				destinationAddress,
				getSessionManager().getNextSequenceNumber(),
				msgRefNum,
				i,
				numberOfSegments,
				shortMessage
			);
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
				byte[] messageBytes = SmppCharset.encodeUcs2(shortMessage);

				DeliverSm pdu = SmppPduUtils.createDeliverSmWithSarTlv(
					sourceAddress,
					destinationAddress,
					getSessionManager().getNextSequenceNumber(),
					msgRefNum,
					i,
					numberOfSegments,
					messageBytes
				);
				getDeliverSender().scheduleDelivery(new PduRequestRecord(pdu, 1000, 5000));
			}
		}
	}

}
