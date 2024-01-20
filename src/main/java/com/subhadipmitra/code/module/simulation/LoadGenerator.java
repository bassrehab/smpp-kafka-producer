package com.subhadipmitra.code.module.simulation;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.*;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Load generator for SMPP traffic simulation.
 * Generates configurable message rates for performance testing.
 */
public class LoadGenerator {
    private static final Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final int connectionCount;
    private final int messagesPerSecond;
    private final int durationSeconds;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final SmppClient smppClient;
    private final CopyOnWriteArrayList<SmppSession> sessions;
    private final Random random;

    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesSuccess = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);

    private volatile boolean running = false;

    /**
     * Create a new LoadGenerator.
     *
     * @param host              SMPP server host
     * @param port              SMPP server port
     * @param systemId          SMPP system ID for binding
     * @param password          SMPP password
     * @param connectionCount   Number of concurrent SMPP connections
     * @param messagesPerSecond Target message rate per second
     * @param durationSeconds   Test duration in seconds
     */
    public LoadGenerator(String host, int port, String systemId, String password,
                         int connectionCount, int messagesPerSecond, int durationSeconds) {
        this.host = host;
        this.port = port;
        this.systemId = systemId;
        this.password = password;
        this.connectionCount = connectionCount;
        this.messagesPerSecond = messagesPerSecond;
        this.durationSeconds = durationSeconds;

        this.executor = Executors.newFixedThreadPool(connectionCount * 2);
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.smppClient = new DefaultSmppClient();
        this.sessions = new CopyOnWriteArrayList<>();
        this.random = new Random();
    }

    /**
     * Start the load test.
     */
    public void start() throws Exception {
        running = true;
        logger.info("Starting load generator: {} connections, {} msg/s, {} seconds",
                connectionCount, messagesPerSecond, durationSeconds);

        // Establish connections
        for (int i = 0; i < connectionCount; i++) {
            try {
                SmppSession session = createSession(i);
                sessions.add(session);
                logger.info("Connection {} established", i);
            } catch (Exception e) {
                logger.error("Failed to establish connection {}", i, e);
            }
        }

        if (sessions.isEmpty()) {
            throw new RuntimeException("No connections could be established");
        }

        // Calculate delay between messages to achieve target rate
        long delayMicros = 1_000_000L / messagesPerSecond;

        // Start message generation
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        // Schedule periodic stats logging
        scheduler.scheduleAtFixedRate(this::logStats, 1, 1, TimeUnit.SECONDS);

        // Generate messages
        while (running && System.currentTimeMillis() < endTime) {
            if (!sessions.isEmpty()) {
                SmppSession session = sessions.get(random.nextInt(sessions.size()));
                executor.submit(() -> sendMessage(session));
            }

            // Rate limiting
            long sleepNanos = delayMicros * 1000;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        stop();
        printFinalStats(startTime);
    }

    /**
     * Stop the load test.
     */
    public void stop() {
        running = false;

        // Close all sessions
        for (SmppSession session : sessions) {
            try {
                session.unbind(5000);
                session.close();
            } catch (Exception e) {
                logger.debug("Error closing session", e);
            }
        }
        sessions.clear();

        // Shutdown executors
        scheduler.shutdownNow();
        executor.shutdownNow();
        smppClient.destroy();

        logger.info("Load generator stopped");
    }

    private SmppSession createSession(int index) throws SmppTimeoutException,
            SmppChannelException, InterruptedException, UnrecoverablePduException {
        SmppSessionConfiguration config = new SmppSessionConfiguration();
        config.setWindowSize(50);
        config.setName("LoadGen-" + index);
        config.setType(SmppBindType.TRANSCEIVER);
        config.setHost(host);
        config.setPort(port);
        config.setConnectTimeout(10000);
        config.setSystemId(systemId);
        config.setPassword(password);
        config.setRequestExpiryTimeout(30000);
        config.setWindowMonitorInterval(15000);
        config.setCountersEnabled(true);

        return smppClient.bind(config);
    }

    private void sendMessage(SmppSession session) {
        if (!running || session == null || !session.isBound()) {
            return;
        }

        try {
            SubmitSm submit = createSubmitSm();
            SubmitSmResp response = session.submit(submit, 5000);

            messagesSent.incrementAndGet();
            if (response.getCommandStatus() == SmppConstants.STATUS_OK) {
                messagesSuccess.incrementAndGet();
            } else {
                messagesFailed.incrementAndGet();
                logger.debug("Submit failed with status: {}", response.getCommandStatus());
            }
        } catch (Exception e) {
            messagesSent.incrementAndGet();
            messagesFailed.incrementAndGet();
            logger.debug("Error sending message: {}", e.getMessage());
        }
    }

    private SubmitSm createSubmitSm() throws SmppInvalidArgumentException {
        SubmitSm submit = new SubmitSm();

        // Source address
        submit.setSourceAddress(new Address((byte) 0x01, (byte) 0x01, generatePhoneNumber()));

        // Destination address
        submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, generatePhoneNumber()));

        // Message content: KEYWORD,VALUE,EXTRADATA format
        String keyword = "TEST" + random.nextInt(100);
        String value = String.valueOf(random.nextInt(10000));
        String extra = "LOAD_TEST";
        String message = keyword + "," + value + "," + extra;

        submit.setShortMessage(CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM));
        submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);

        return submit;
    }

    private String generatePhoneNumber() {
        return "+1" + String.format("%010d", Math.abs(random.nextInt()));
    }

    private void logStats() {
        long sent = messagesSent.get();
        long success = messagesSuccess.get();
        long failed = messagesFailed.get();
        double successRate = sent > 0 ? (success * 100.0 / sent) : 0;

        logger.info("Stats: sent={}, success={}, failed={}, successRate={:.2f}%",
                sent, success, failed, successRate);
    }

    private void printFinalStats(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        long sent = messagesSent.get();
        long success = messagesSuccess.get();
        long failed = messagesFailed.get();
        double actualRate = sent * 1000.0 / duration;
        double successRate = sent > 0 ? (success * 100.0 / sent) : 0;

        logger.info("=== Final Statistics ===");
        logger.info("Duration: {} ms", duration);
        logger.info("Messages sent: {}", sent);
        logger.info("Messages success: {}", success);
        logger.info("Messages failed: {}", failed);
        logger.info("Actual rate: {:.2f} msg/s", actualRate);
        logger.info("Success rate: {:.2f}%", successRate);
        logger.info("========================");
    }

    public long getMessagesSent() {
        return messagesSent.get();
    }

    public long getMessagesSuccess() {
        return messagesSuccess.get();
    }

    public long getMessagesFailed() {
        return messagesFailed.get();
    }

    /**
     * Main entry point for standalone load testing.
     */
    public static void main(String[] args) {
        // Default configuration
        String host = System.getProperty("smpp.host", "localhost");
        int port = Integer.parseInt(System.getProperty("smpp.port", "2775"));
        String systemId = System.getProperty("smpp.systemId", "smppclient1");
        String password = System.getProperty("smpp.password", "password");
        int connections = Integer.parseInt(System.getProperty("load.connections", "2"));
        int rate = Integer.parseInt(System.getProperty("load.rate", "100"));
        int duration = Integer.parseInt(System.getProperty("load.duration", "60"));

        LoadGenerator generator = new LoadGenerator(
                host, port, systemId, password, connections, rate, duration);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(generator::stop));

        try {
            generator.start();
        } catch (Exception e) {
            logger.error("Load test failed", e);
            System.exit(1);
        }
    }
}
