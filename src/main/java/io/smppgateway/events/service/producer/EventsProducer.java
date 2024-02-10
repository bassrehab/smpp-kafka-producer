package io.smppgateway.events.service.producer;

import com.cloudhopper.smpp.pdu.SubmitSm;
import io.smppgateway.events.service.eventrecord.EventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;

public class EventsProducer implements Callable<EventRecord> {

	/** Logger Instance */
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private String producerName;
	private SubmitSm event;
	
	public EventsProducer(String prodName, SubmitSm evt) {
		this.producerName = prodName;
		this.event = evt;
	}

	@Override
	public EventRecord call() throws Exception {
		logger.debug("CurrentProducer[name="+ this.producerName +", msisdn="+ this.event.getDestAddress().getAddress()  +", at="+ new Date());

		EventRecord evtRecord = new EventRecord();
		evtRecord.setEvt(event);

		return evtRecord;
	}
}
