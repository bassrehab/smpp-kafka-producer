package com.subhadipmitra.code.module.init;

import com.subhadipmitra.code.module.events.service.completionservice.CompletionServiceProvider;
import com.subhadipmitra.code.module.producer.source.SMPPProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 26/09/17.
 */
public class ShutDownCleanup extends Thread {
    /** Instantiate Logger */
    private static final Logger logger = LoggerFactory.getLogger(ShutDownCleanup.class);

    /** SMPP Server Setup Instance */
    private final ServerMain server;

    /** SMPP Kafka Producer Setup Instance */
    private final SMPPProducer smppProducer;

    ShutDownCleanup(ServerMain server, SMPPProducer smppProducer) {
        this.server = server;
        this.smppProducer = smppProducer;
    }

    @Override
    public void run() {
        logger.info("Received shutdown signal..");

        try {
            server.stop();
            server.printMetrics();
            server.destroy();
        } catch (Exception e) {
            logger.error("Error during server shutdown", e);
        }

        CompletionServiceProvider.shutdown();

        smppProducer.shutdown();

        logger.info("Finished pre-shutdown tasks");
    }
}
