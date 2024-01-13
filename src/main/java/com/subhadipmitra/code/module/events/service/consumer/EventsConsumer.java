package com.subhadipmitra.code.module.events.service.consumer;

import com.subhadipmitra.code.module.metrics.MetricsRegistry;
import com.subhadipmitra.code.module.models.SMS;
import com.subhadipmitra.code.module.events.service.eventrecord.EventRecord;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.subhadipmitra.code.module.init.Main.*;


public class EventsConsumer implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private final MetricsRegistry metrics = MetricsRegistry.getInstance();

	private String consumerName;
	private CompletionService service;
	private volatile boolean run = true;

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
        while (run) {
            logger.debug("Consumer waiting for Event Record: {} at {}", this.consumerName, new Date());
            try {
                Future<EventRecord> fp = service.take();

                // Update metrics - message received from queue
                metrics.incrementSmppReceived();
                metrics.decrementQueueSize();
                METRICS_SMPP_CONSUMER_EVENTS_RECEIVED.incrementAndGet();

                // Start processing timer
                Timer.Sample processingTimer = metrics.startSmppTimer();

                EventRecord evt = fp.get();
                evt.setQueueEndTime(); // Stop Queue timer

                // Record queue wait time in metrics
                metrics.recordQueueWaitTime(evt.getQueueWaitTime());

                // Send to Kafka
                SMS sms = new SMS(evt);
                smppProducer.sendMessage(sms);

                // Record processing time and update counters
                metrics.recordSmppProcessingTime(processingTimer);
                metrics.incrementSmppProcessed();
                METRICS_SMPP_CONSUMER_EVENTS_PROCESSED.incrementAndGet();

                logger.debug("SMPP Queue[evtSent={}, evtReceived={}, evtProcessed={}, queueWaitTime={} ms]",
                        METRICS_SMPP_PRODUCER_EVENTS_SENT.get(),
                        METRICS_SMPP_CONSUMER_EVENTS_RECEIVED.get(),
                        METRICS_SMPP_CONSUMER_EVENTS_PROCESSED.get(),
                        evt.getQueueWaitTime() / 1_000_000);

			} catch (InterruptedException | ExecutionException e) {
                logger.error("Error processing event in consumer: {}", consumerName, e);
                metrics.incrementSmppFailed();
            }
        }
	}


    /** Shutdown */
    public void shutdown(){
        this.run=false;

    }



}
