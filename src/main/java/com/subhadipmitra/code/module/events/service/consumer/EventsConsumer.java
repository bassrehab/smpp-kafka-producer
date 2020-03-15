package com.subhadipmitra.code.module.events.service.consumer;


import com.subhadipmitra.code.module.models.SMS;
import com.subhadipmitra.code.module.events.service.eventrecord.EventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.subhadipmitra.code.module.init.Main.*;


public class EventsConsumer implements Runnable {
	/** Logger Instance */
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	/** Consumer Name */
	private String consumerName;

	/** Completion Service */
	private CompletionService service;


    /** Run Latch */
    private boolean run = true;

	/** Constructor */

	public EventsConsumer(String consumerName, CompletionService service) {
		this.consumerName = consumerName;
		this.service = service;

	}


	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public CompletionService getService() {
		return service;
	}

	public void setService(CompletionService service) {
		this.service = service;
	}


	@Override
	public void run() {
        while(run){
            logger.debug("Consumer waiting for Event Record: "+ this.consumerName + " at "+ new Date());
            try {



                Future<EventRecord> fp = service.take();

                METRICS_SMPP_CONSUMER_EVENTS_RECEIVED += 1;
				EventRecord evt = fp.get();
				evt.setQueueEndTime(); // Stop Queue timer



				// Send to Kafka
				SMS sms = new SMS(evt);

				smppProducer.sendMessage(sms);


                METRICS_SMPP_CONSUMER_EVENTS_PROCESSED += 1;

                logger.debug("SMPP Queue[evtSent="+ METRICS_SMPP_PRODUCER_EVENTS_SENT
                        +", evtReceived=" + METRICS_SMPP_CONSUMER_EVENTS_RECEIVED
                        + ", evtProcessed="+ METRICS_SMPP_CONSUMER_EVENTS_PROCESSED
                        +", queueWaitTime=" + evt.getQueueWaitTime() / 1000000 + " ms ]");


			} catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
	}


    /** Shutdown */
    public void shutdown(){
        this.run=false;

    }



}
