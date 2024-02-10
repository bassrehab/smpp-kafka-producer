package io.smppgateway.server;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import io.smppgateway.controller.core.DeliveryReceiptScheduler;
import io.smppgateway.controller.core.ResponseMessageIdGenerator;
import io.smppgateway.controller.auto.DelayedRequestSenderImpl;
import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import io.smppgateway.events.service.producer.EventsProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import static io.smppgateway.init.Main.METRICS_SMPP_PRODUCER_EVENTS_SENT;
import static io.smppgateway.init.Main.compSMPPService;
import static io.smppgateway.init.ServerMain.isTestMode;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 *
 */
public class SmscSmppSessionHandler extends DefaultSmppSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SmscSmppSessionHandler.class);

    private WeakReference<SmppSession> sessionRef;

    private DelayedRequestSenderImpl deliverSender;

    private ResponseMessageIdGenerator messageIdGenerator;

    private DeliveryReceiptScheduler deliveryReceiptScheduler;




    public SmscSmppSessionHandler(SmppServerSession session, SmscGlobalConfiguration config) {
        this.sessionRef = new WeakReference<SmppSession>(session);
        this.deliverSender = config.getDeliverSender();
        this.messageIdGenerator = config.getMessageIdGenerator();
        this.deliveryReceiptScheduler = config.getDeliveryReceiptScheduler();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        SmppSession session = sessionRef.get();

        if (pduRequest instanceof SubmitSm) {
            SubmitSm submitSm = (SubmitSm) pduRequest;
            SubmitSmResp submitSmResp = submitSm.createResponse();
            long messageId = messageIdGenerator.getNextMessageId();
            submitSmResp.setMessageId(FormatUtils.formatAsHex(messageId));


            try {

                if(!isTestMode){
                    // Send the SubmitSm Obj to Queue, and further processing
                    EventsProducer prod = new EventsProducer("Producer_" + METRICS_SMPP_PRODUCER_EVENTS_SENT, submitSm );
                    METRICS_SMPP_PRODUCER_EVENTS_SENT += 1;
                    compSMPPService.submit(prod);
                }

                // We can not wait in this thread!!
                // It would block handling of other messages and performance would drop drastically!!
                // create and enqueue delivery receipt
                if (submitSm.getRegisteredDelivery() > 0 && deliverSender != null) {
                    DeliveryReceiptRecord record = new DeliveryReceiptRecord(session, submitSm, messageId);
                    record.setDeliverTime(deliveryReceiptScheduler.getDeliveryTimeMillis());
                    deliverSender.scheduleDelivery(record);
                }
            } catch (Exception e) {
                logger.error("Error when handling submit", e);
            }

            //submitSmResp.setCommandStatus(SmppConstants.STATUS_X_T_APPN);
            return submitSmResp;
            //return null;
        } else if (pduRequest instanceof Unbind) {
            //session.destroy();  
            // TO DO refine, this throws exceptions
            session.unbind(1000);
            return pduRequest.createResponse();
        }

        return pduRequest.createResponse();
    }

    @Override
    public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
        if (pduAsyncResponse.getResponse().getCommandStatus() != SmppConstants.STATUS_OK) {
            // TODO
            // error, resend the request again?
            //pduAsyncResponse.getRequest().setReferenceObject(value)
        }
    }

}
