package com.subhadipmitra.code.module.init;

import com.subhadipmitra.code.module.events.service.completionservice.CompletionServiceProvider;
import com.subhadipmitra.code.module.events.service.consumer.EventsConsumer;
import com.subhadipmitra.code.module.metrics.MetricsRegistry;
import com.subhadipmitra.code.module.metrics.MetricsServer;
import com.subhadipmitra.code.module.producer.source.SMPPProducer;
import com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhadipmitra.code.module.config.loader.ConfigLoaderExternal;
import com.subhadipmitra.code.module.common.utilities.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionService;
import java.util.concurrent.atomic.AtomicLong;

import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVICE_EVENTS_NAME;
import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVICE_EVENTS_NUM_CONSUMERS;
import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.METRICS_SERVER_PORT;
import static com.subhadipmitra.code.module.init.ServerMain.parseParameters;


/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 09/10/17.
 *
 */

public class Main {
    /** Logger Instance */
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /** Utilities Instance */
    public static final Utilities utilities = new Utilities();

    /** Initialize the mapper instance (thread-safe, immutable) */
    public static final ObjectMapper mapper = new ObjectMapper();

    /** Events Producer Consumer Completion Service */
    public static CompletionService compSMPPService;

    /** Counter for EventsProducer Events Number, also shared by Events Consumer (thread-safe) */
    public static final AtomicLong METRICS_SMPP_PRODUCER_EVENTS_SENT = new AtomicLong(0L);

    /** Metric (thread-safe) */
    public static final AtomicLong METRICS_SMPP_CONSUMER_EVENTS_RECEIVED = new AtomicLong(0L);

    /** Metric (thread-safe) */
    public static final AtomicLong METRICS_SMPP_CONSUMER_EVENTS_PROCESSED = new AtomicLong(0L);


    /** SMPP Server Setup Instance */
    public ServerMain server;


    @Value("${config.smpp}")
    private String configsmpp;

    /** Config Instance */
    private static ConfigLoaderExternal cfg;

    private static String cfg_smpp;

    @Value("${config.properties}")
    private String configproperties;

    /** Create SMPP Producer **/
    public static SMPPProducer smppProducer;

    /** Metrics Server **/
    public static MetricsServer metricsServer;

    /** Main entry point to application */
    public static void main(String[] args) {
        new Main().start(args);
    }

    /** Load Configs, Start Producer, and Start Webserver */
    private void start(String[] args) {

        ApplicationContext ctx = new GenericApplicationContext();
        Environment env = ctx.getEnvironment();

        // Create Config Object.
        cfg = new ConfigLoaderExternal(env.getProperty("config.properties"));
        cfg_smpp = env.getProperty("config.smpp"); //context.xml location

        // Create the Global Config Instances
        new ConfigurationsSourceSMPP(cfg);

        // Initialize Metrics Registry (singleton)
        MetricsRegistry.getInstance();
        logger.info("Metrics registry initialized");

        // Start Metrics Server
        metricsServer = new MetricsServer(METRICS_SERVER_PORT);
        metricsServer.start();

        // Instantiate Completion Service.
        new CompletionServiceProvider();
        compSMPPService = CompletionServiceProvider.getCompletionservice(SMPP_SERVICE_EVENTS_NAME);

        /* Parse the Parameters */
        if (!parseParameters(args)) {
            System.exit(0);
        }

        // Start the Server Setup.
        try {
            startServer(cfg_smpp);
        } catch (Exception e) {
            logger.error("Got Error while starting SMPP Servers", e);
        }

        // Create the SMPP Producer
        smppProducer = new SMPPProducer();

        /* Start Consumer Service */
        for (int i = 0; i < SMPP_SERVICE_EVENTS_NUM_CONSUMERS; i++) {
            new Thread(new EventsConsumer(SMPP_SERVICE_EVENTS_NAME + "_" + i, compSMPPService)).start();
        }

        /* Register Shutdown Hook */
        Runtime.getRuntime().addShutdownHook(new ShutDownCleanup(server, smppProducer, metricsServer));
    }


    protected void startServer(String cfg_smpp) throws Exception {
        this.server = new ServerMain(cfg_smpp, false);
        this.server.start();
    }
}
