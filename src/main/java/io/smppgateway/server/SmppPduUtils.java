package io.smppgateway.server;

import io.smppgateway.smpp.charset.SmppCharset;
import io.smppgateway.smpp.pdu.DeliverSm;
import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.DataCoding;
import io.smppgateway.smpp.types.EsmClass;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating SMPP PDUs.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public class SmppPduUtils {

    // SMPP message state constants
    public static final byte STATE_DELIVERED = 2;

    // SMPP ESM class UDHI mask
    public static final byte ESM_CLASS_UDHI_MASK = 0x40;

    public static DeliverSm createDeliveryReceipt(DeliveryReceiptRecord deliveryReceiptRecord, int sequenceNumber) {
        // Build delivery receipt message text
        String shortMessage = buildDeliveryReceiptText(
            FormatUtils.formatAsDec(deliveryReceiptRecord.getMessageId()),
            1, 1,
            deliveryReceiptRecord.getSubmitDate(),
            LocalDateTime.now(),
            STATE_DELIVERED, 0, "-"
        );

        byte[] messageBytes = SmppCharset.encodeUcs2(shortMessage);

        List<Tlv> tlvs = new ArrayList<>();
        // NOTE: VERY IMPORTANT -- THIS IS A C-STRING!
        tlvs.add(Tlv.ofString(TlvTag.RECEIPTED_MESSAGE_ID,
            FormatUtils.formatAsHex(deliveryReceiptRecord.getMessageId())));
        tlvs.add(Tlv.ofByte(TlvTag.MESSAGE_STATE, STATE_DELIVERED));

        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(deliveryReceiptRecord.getSourceAddress())
            .destAddress(deliveryReceiptRecord.getDestinationAddress())
            .asDeliveryReceipt()
            .protocolId((byte) 0x00)
            .priorityFlag((byte) 0x00)
            .dataCoding(DataCoding.UCS2)
            .shortMessage(messageBytes)
            .addTlv(tlvs.get(0))
            .addTlv(tlvs.get(1))
            .build();
    }

    public static DeliverSm createDeliverSm(Address sourceAddress, Address destinationAddress, int sequenceNumber) {
        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .protocolId((byte) 0x00)
            .priorityFlag((byte) 0x00)
            .dataCoding(DataCoding.DEFAULT)
            .build();
    }

    /**
     * Creates a deliver_sm with UDH 00 header for segmented messages.
     * Based on http://memoirniche.wordpress.com/2010/04/10/smpp-submit-pdu/
     **/
    public static DeliverSm createDeliverSmWithUdh00(Address sourceAddress, Address destinationAddress,
                                                      int sequenceNumber, int msgRefNum, int segmentNum,
                                                      int totalSegmentCount, String shortMessage) {
        byte[] udhHeader = new byte[6];
        udhHeader[0] = 0x05; // total length of data in UDH
        udhHeader[1] = 0x00; // IE identifier for concatenated messages
        udhHeader[2] = 0x03; // length of data in IE
        udhHeader[3] = (byte) msgRefNum;
        udhHeader[4] = (byte) totalSegmentCount;
        udhHeader[5] = (byte) segmentNum;

        byte[] message = SmppCharset.encodeUcs2(shortMessage);
        ByteBuffer bb = ByteBuffer.allocate(udhHeader.length + message.length);
        bb.put(udhHeader);
        bb.put(message);

        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .esmClass(EsmClass.fromByte(ESM_CLASS_UDHI_MASK))
            .dataCoding(DataCoding.UCS2)
            .shortMessage(bb.array())
            .build();
    }

    /**
     * Creates a deliver_sm with UDH 08 header for segmented messages.
     **/
    public static DeliverSm createDeliverSmWithUdh08(Address sourceAddress, Address destinationAddress,
                                                      int sequenceNumber, int msgRefNum, int segmentNum,
                                                      int totalSegmentCount, String shortMessage) {
        byte[] udhHeader = new byte[7];
        udhHeader[0] = 0x06; // total length of data in UDH
        udhHeader[1] = 0x08; // IE identifier for concatenated messages
        udhHeader[2] = 0x05; // length of data in IE
        byte[] unsignedShortMsgRefNum = intToUnsignedShort(msgRefNum);
        udhHeader[3] = unsignedShortMsgRefNum[0];
        udhHeader[4] = unsignedShortMsgRefNum[1];
        udhHeader[5] = (byte) totalSegmentCount;
        udhHeader[6] = (byte) segmentNum;

        byte[] message = SmppCharset.encodeUcs2(shortMessage);
        ByteBuffer bb = ByteBuffer.allocate(udhHeader.length + message.length);
        bb.put(udhHeader);
        bb.put(message);

        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .esmClass(EsmClass.fromByte(ESM_CLASS_UDHI_MASK))
            .dataCoding(DataCoding.UCS2)
            .shortMessage(bb.array())
            .build();
    }

    /**
     * Creates a deliver_sm with SAR TLV optional parameters.
     **/
    public static DeliverSm createDeliverSmWithSarTlv(Address sourceAddress, Address destinationAddress,
                                                       int sequenceNumber, int msgRefNum, int segmentNum,
                                                       int totalSegmentCount, byte[] messageBytes) {
        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .shortMessage(messageBytes)
            .addTlv(new Tlv(TlvTag.SAR_MSG_REF_NUM, intToUnsignedShort(msgRefNum)))
            .addTlv(new Tlv(TlvTag.SAR_SEGMENT_SEQNUM, intToUnsignedByte(segmentNum)))
            .addTlv(new Tlv(TlvTag.SAR_TOTAL_SEGMENTS, intToUnsignedByte(totalSegmentCount)))
            .build();
    }

    /**
     * Creates a deliver_sm with SAR TLV optional parameters and custom data coding.
     **/
    public static DeliverSm createDeliverSmWithSarTlvAndDataCoding(Address sourceAddress, Address destinationAddress,
                                                                     int sequenceNumber, int msgRefNum, int segmentNum,
                                                                     int totalSegmentCount, byte[] messageBytes,
                                                                     DataCoding dataCoding) {
        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .shortMessage(messageBytes)
            .dataCoding(dataCoding)
            .addTlv(new Tlv(TlvTag.SAR_MSG_REF_NUM, intToUnsignedShort(msgRefNum)))
            .addTlv(new Tlv(TlvTag.SAR_SEGMENT_SEQNUM, intToUnsignedByte(segmentNum)))
            .addTlv(new Tlv(TlvTag.SAR_TOTAL_SEGMENTS, intToUnsignedByte(totalSegmentCount)))
            .build();
    }

    /**
     * Creates a deliver_sm with message_payload TLV for long messages.
     **/
    public static DeliverSm createDeliverSmWithMessagePayload(Address sourceAddress, Address destinationAddress,
                                                                int sequenceNumber, int msgRefNum, int segmentNum,
                                                                int totalSegmentCount, byte[] messagePayload,
                                                                DataCoding dataCoding) {
        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .dataCoding(dataCoding)
            .addTlv(new Tlv(TlvTag.MESSAGE_PAYLOAD, messagePayload))
            .addTlv(new Tlv(TlvTag.SAR_MSG_REF_NUM, intToUnsignedShort(msgRefNum)))
            .addTlv(new Tlv(TlvTag.SAR_SEGMENT_SEQNUM, intToUnsignedByte(segmentNum)))
            .addTlv(new Tlv(TlvTag.SAR_TOTAL_SEGMENTS, intToUnsignedByte(totalSegmentCount)))
            .build();
    }

    /**
     * Creates a deliver_sm with message_payload TLV.
     **/
    public static DeliverSm createDeliverSmWithPayload(Address sourceAddress, Address destinationAddress,
                                                        int sequenceNumber, byte[] messagePayload) {
        return DeliverSm.builder()
            .sequenceNumber(sequenceNumber)
            .sourceAddress(sourceAddress)
            .destAddress(destinationAddress)
            .addTlv(new Tlv(TlvTag.MESSAGE_PAYLOAD, messagePayload))
            .build();
    }

    public static byte[] intToUnsignedShort(int value) {
        byte[] ret = new byte[2];
        ret[0] = (byte) ((value & 0xff00) >>> 8);
        ret[1] = (byte) (value & 0xff);
        return ret;
    }

    public static byte[] intToUnsignedByte(int value) {
        byte[] ret = new byte[1];
        ret[0] = (byte) value;
        return ret;
    }

    public static byte[] convertOptionalStringToCOctet(String str) {
        if (str == null) return null;

        byte[] value = str.getBytes();
        byte[] nullTerminatedValue = new byte[value.length + 1];
        System.arraycopy(value, 0, nullTerminatedValue, 0, value.length);
        nullTerminatedValue[value.length] = 0;
        return nullTerminatedValue;
    }

    /**
     * Builds a delivery receipt text in SMPP standard format.
     */
    private static String buildDeliveryReceiptText(String messageId, int submitted, int delivered,
                                                    LocalDateTime submitDate, LocalDateTime doneDate,
                                                    byte state, int errorCode, String text) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyMMddHHmm");
        String stateStr = switch (state) {
            case 1 -> "ENROUTE";
            case 2 -> "DELIVRD";
            case 3 -> "EXPIRED";
            case 4 -> "DELETED";
            case 5 -> "UNDELIV";
            case 6 -> "ACCEPTD";
            case 7 -> "UNKNOWN";
            case 8 -> "REJECTD";
            default -> "UNKNOWN";
        };

        return String.format("id:%s sub:%03d dlvrd:%03d submit date:%s done date:%s stat:%s err:%03d text:%s",
            messageId, submitted, delivered,
            submitDate != null ? submitDate.format(fmt) : "0000000000",
            doneDate != null ? doneDate.format(fmt) : "0000000000",
            stateStr, errorCode, text != null ? text : "");
    }
}
