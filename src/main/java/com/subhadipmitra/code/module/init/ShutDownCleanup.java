package com.subhadipmitra.code.module.init;

import com.subhadipmitra.code.module.events.service.completionservice.CompletionServiceProvider;
import com.subhadipmitra.code.module.producer.source.SMPPProducer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 26/09/17.
 */
public class ShutDownCleanup extends Thread {
    /** Instantiate Logger */
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    /** SMPP Server Setup Instance */
    private static ServerMain server;

    /** SMPP Kafka Producer Setup Instance */
    private static SMPPProducer smppProducer;

    ShutDownCleanup(ServerMain server, SMPPProducer smppProducer){
        ShutDownCleanup.server = server;
        ShutDownCleanup.smppProducer = smppProducer;
    }
    @Override
    public void run(){

        logger.info("Received shutdown signal..");

        try {
            server.stop();
            server.printMetrics();
            server.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CompletionServiceProvider.shutdown();

        smppProducer.shutdown();

        logger.info("Finished pre-shutdown tasks");

    }
}
