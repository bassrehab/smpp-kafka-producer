package io.smppgateway.simulation;

import com.cloudhopper.commons.charset.CharsetUtil;
import io.smppgateway.smpp.client.SmppClient;
import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.pdu.SubmitSmResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.DataCoding;
import io.smppgateway.smpp.types.RegisteredDelivery;
import io.smppgateway.smpp.types.SmppBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
    private final CopyOnWriteArrayList<SmppClientSession> sessions;
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
                SmppClientSession session = createSession(i);
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
                SmppClientSession session = sessions.get(random.nextInt(sessions.size()));
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
        for (SmppClientSession session : sessions) {
            try {
                session.unbind();
                session.close();
            } catch (Exception e) {
                logger.debug("Error closing session", e);
            }
        }
        sessions.clear();

        // Shutdown executors
        scheduler.shutdownNow();
        executor.shutdownNow();

        logger.info("Load generator stopped");
    }

    private SmppClientSession createSession(int index) throws Exception {
        SmppClient client = SmppClient.builder()
            .host(host)
            .port(port)
            .systemId(systemId)
            .password(password)
            .bindType(SmppBindType.TRANSCEIVER)
            .windowSize(50)
            .connectTimeout(Duration.ofSeconds(10))
            .requestTimeout(Duration.ofSeconds(30))
            .build();

        return client.connect();
    }

    private void sendMessage(SmppClientSession session) {
        if (!running || session == null || !session.isBound()) {
            return;
        }

        try {
            SubmitSm submit = createSubmitSm();
            SubmitSmResp response = session.submitSm(submit, Duration.ofSeconds(5));

            messagesSent.incrementAndGet();
            if (response.commandStatus() == CommandStatus.ESME_ROK) {
                messagesSuccess.incrementAndGet();
            } else {
                messagesFailed.incrementAndGet();
                logger.debug("Submit failed with status: {}", response.commandStatus());
            }
        } catch (Exception e) {
            messagesSent.incrementAndGet();
            messagesFailed.incrementAndGet();
            logger.debug("Error sending message: {}", e.getMessage());
        }
    }

    private SubmitSm createSubmitSm() {
        // Source address
        Address sourceAddress = new Address((byte) 0x01, (byte) 0x01, generatePhoneNumber());

        // Destination address
        Address destAddress = new Address((byte) 0x01, (byte) 0x01, generatePhoneNumber());

        // Message content: KEYWORD,VALUE,EXTRADATA format
        String keyword = "TEST" + random.nextInt(100);
        String value = String.valueOf(random.nextInt(10000));
        String extra = "LOAD_TEST";
        String message = keyword + "," + value + "," + extra;

        byte[] messageBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);

        return SubmitSm.builder()
            .sourceAddress(sourceAddress)
            .destAddress(destAddress)
            .shortMessage(messageBytes)
            .dataCoding(DataCoding.GSM7)
            .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
            .build();
    }

    private String generatePhoneNumber() {
        return "+1" + String.format("%010d", Math.abs(random.nextInt()));
    }

    private void logStats() {
        long sent = messagesSent.get();
        long success = messagesSuccess.get();
        long failed = messagesFailed.get();
        double successRate = sent > 0 ? (success * 100.0 / sent) : 0;

        logger.info("Stats: sent={}, success={}, failed={}, successRate={}%",
                sent, success, failed, String.format("%.2f", successRate));
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
        logger.info("Actual rate: {} msg/s", String.format("%.2f", actualRate));
        logger.info("Success rate: {}%", String.format("%.2f", successRate));
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
