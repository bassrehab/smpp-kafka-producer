package io.smppgateway.events.service.producer;

import io.smppgateway.events.service.eventrecord.EventRecord;
import io.smppgateway.smpp.pdu.SubmitSm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Producer for SMPP events to be processed by the completion service.
 */
public class EventsProducer implements Callable<EventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(EventsProducer.class);

    private final String producerName;
    private final SubmitSm event;

    public EventsProducer(String prodName, SubmitSm evt) {
        this.producerName = prodName;
        this.event = evt;
    }

    @Override
    public EventRecord call() throws Exception {
        logger.debug("CurrentProducer[name={}, msisdn={}, at={}]",
            producerName, event.destAddress().address(), new Date());

        EventRecord evtRecord = new EventRecord();
        evtRecord.setEvt(event);

        return evtRecord;
    }
}
