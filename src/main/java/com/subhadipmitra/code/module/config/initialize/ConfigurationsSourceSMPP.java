package com.subhadipmitra.code.module.config.initialize;

import com.subhadipmitra.code.module.config.loader.ConfigLoaderExternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 01/09/17.
 */
public class ConfigurationsSourceSMPP {
    /** Logger Instance */
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationsSourceSMPP.class);

    /** Date formatter (thread-safe) */
    private static final DateTimeFormatter JVM_START_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss");

    /** Pseudo JVM start time (thread-safe) */
    public static final String SMPP_SERVICE_JVM_START_TIME = LocalDateTime.now().format(JVM_START_TIME_FORMATTER);

    /** Events Service Specifics */
    public static int SMPP_SERVICE_EVENTS_QUEUE_SIZE;
    public static int SMPP_SERVICE_EVENTS_NUM_CONSUMERS;
    public static int SMPP_SERVICE_EVENTS_EXECUTOR_ALT_POOL_SIZE;
    public static int SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_CORE_POOL_SIZE;
    public static int SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MAXIMUM_POOL_SIZE;
    public static int SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_KEEP_ALIVE_TIME_SECS;
    public static boolean SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_ENABLE_RETRIES;
    public static int SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MONITOR_DELAY_SECS;
    public static String SMPP_SERVICE_EVENTS_NAME;


    /** Service Config Definitions */
    public static String SOURCE_SMPP_KAFKA_PRODUCER_BROKERS;
    public static List<String> SOURCE_SMPP_KAFKA_PRODUCER_TOPICS;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_GROUP_ID;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_ACKS;
    public static int SOURCE_SMPP_KAFKA_PRODUCER_RETRIES;
    public static int SOURCE_SMPP_KAFKA_PRODUCER_BATCH_SIZE;
    public static int SOURCE_SMPP_KAFKA_PRODUCER_LINGER_MS;
    public static int SOURCE_SMPP_KAFKA_PRODUCER_BUFFER_MEMORY;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_KEY_SERIALIZER;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_VALUE_SERIALIZER;
    public static boolean SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_ENABLED;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_SECURITY_PROTOCOL;
    public static String SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME;

    /** Service Config Definitions */
    public static String TELEMETRY_KAFKA_PRODUCER_BROKERS;
    public static List<String> TELEMETRY_KAFKA_PRODUCER_TOPICS;
    public static String TELEMETRY_KAFKA_PRODUCER_GROUP_ID;
    public static String TELEMETRY_KAFKA_PRODUCER_ACKS;
    public static int TELEMETRY_KAFKA_PRODUCER_RETRIES;
    public static int TELEMETRY_KAFKA_PRODUCER_BATCH_SIZE;
    public static int TELEMETRY_KAFKA_PRODUCER_LINGER_MS;
    public static int TELEMETRY_KAFKA_PRODUCER_BUFFER_MEMORY;
    public static String TELEMETRY_KAFKA_PRODUCER_KEY_SERIALIZER;
    public static String TELEMETRY_KAFKA_PRODUCER_VALUE_SERIALIZER;
    public static boolean TELEMETRY_KAFKA_PRODUCER_KERBEROS_ENABLED;
    public static String TELEMETRY_KAFKA_PRODUCER_SECURITY_PROTOCOL;
    public static String TELEMETRY_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME;



    /** Module Specifics */
    public static String SOURCE_SMPP_SERVICE_BATCH_WINDOW;
    public static String SOURCE_SMPP_SMS_DELIMITER;
    public static int SMPP_SERVER_DELAYED_REQUEST_TIMEOUT_MS;
    public static int SMPP_SERVER_MIN_DELAYED_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS;
    public static int SMPP_SERVER_DELTA_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS;
    public static int SMPP_SERVER_BASE_SENDER_SEND_TIMEOUT_MS;
    public static String SMPP_SERVER_SESSION_PASSWORD;


    public ConfigurationsSourceSMPP(ConfigLoaderExternal cfg) {

        logger.info("Preparing configurations defaults SMPP Source..");

        SMPP_SERVICE_EVENTS_QUEUE_SIZE = Integer.parseInt(cfg.getProperty("smpp.service.events.queue.size"));
        SMPP_SERVICE_EVENTS_NUM_CONSUMERS = Integer.parseInt(cfg.getProperty("smpp.service.events.num.consumers"));
        SMPP_SERVICE_EVENTS_EXECUTOR_ALT_POOL_SIZE = Integer.parseInt(cfg.getProperty("smpp.service.events.executor.alt.pool.size"));
        SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_CORE_POOL_SIZE = Integer.parseInt(cfg.getProperty("smpp.service.events.executor.main.core.pool.size"));
        SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MAXIMUM_POOL_SIZE = Integer.parseInt(cfg.getProperty("smpp.service.events.executor.main.maximum.pool.size"));
        SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_KEEP_ALIVE_TIME_SECS = Integer.parseInt(cfg.getProperty("smpp.service.events.executor.main.keep.alive.secs"));
        SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_ENABLE_RETRIES = Boolean.parseBoolean(cfg.getProperty("smpp.service.events.executor.main.enable.retries"));
        SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MONITOR_DELAY_SECS = Integer.parseInt(cfg.getProperty("smpp.service.events.executor.main.monitor.delay.secs"));
        SMPP_SERVICE_EVENTS_NAME = cfg.getProperty("smpp.service.events.name");



        SMPP_SERVER_DELAYED_REQUEST_TIMEOUT_MS = Integer.parseInt(cfg.getProperty("smpp.server.delayed.request.timeout.ms"));
        SMPP_SERVER_MIN_DELAYED_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS = Integer.parseInt(cfg.getProperty("smpp.server.min.delayed.random.delivery.receipt.scheduler.ms"));
        SMPP_SERVER_DELTA_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS = Integer.parseInt(cfg.getProperty("smpp.server.delta.random.delivery.receipt.scheduler.ms"));
        SMPP_SERVER_BASE_SENDER_SEND_TIMEOUT_MS = Integer.parseInt(cfg.getProperty("smpp.server.base.sender.send.timeout.ms"));
        SMPP_SERVER_SESSION_PASSWORD = cfg.getProperty("smpp.server.session.password");

        /* Load Service Module Configurations */
        SOURCE_SMPP_SMS_DELIMITER = cfg.getProperty("source.smpp.sms.delimiter");
        SOURCE_SMPP_SERVICE_BATCH_WINDOW = cfg.getProperty("source.smpp.batch.window");
        SOURCE_SMPP_KAFKA_PRODUCER_BROKERS = cfg.getProperty("source.smpp.kafka.producer.brokers");
        SOURCE_SMPP_KAFKA_PRODUCER_TOPICS = cfg.getProperty("source.smpp.kafka.producer.topics", ",");
        SOURCE_SMPP_KAFKA_PRODUCER_GROUP_ID = cfg.getProperty("source.smpp.kafka.producer.groupId");
        SOURCE_SMPP_KAFKA_PRODUCER_ACKS = cfg.getProperty("source.smpp.kafka.producer.acks");
        SOURCE_SMPP_KAFKA_PRODUCER_RETRIES = Integer.parseInt(cfg.getProperty("source.smpp.kafka.producer.retries"));
        SOURCE_SMPP_KAFKA_PRODUCER_BATCH_SIZE = Integer.parseInt(cfg.getProperty("source.smpp.kafka.producer.batch.size"));
        SOURCE_SMPP_KAFKA_PRODUCER_LINGER_MS = Integer.parseInt(cfg.getProperty("source.smpp.kafka.producer.linger.ms"));
        SOURCE_SMPP_KAFKA_PRODUCER_BUFFER_MEMORY = Integer.parseInt(cfg.getProperty("source.smpp.kafka.producer.buffer.memory"));
        SOURCE_SMPP_KAFKA_PRODUCER_KEY_SERIALIZER = cfg.getProperty("source.smpp.kafka.producer.key.serializer");
        SOURCE_SMPP_KAFKA_PRODUCER_VALUE_SERIALIZER = cfg.getProperty("source.smpp.kafka.producer.value.serializer");
        SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_ENABLED = Boolean.parseBoolean(cfg.getProperty("source.smpp.kafka.kerberos.enabled"));
        SOURCE_SMPP_KAFKA_PRODUCER_SECURITY_PROTOCOL = cfg.getProperty("source.smpp.kafka.security.protocol");
        SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME = cfg.getProperty("source.smpp.kafka.sasl.kerberos.service.name");

        
         /* Load Service Module Configurations */
        TELEMETRY_KAFKA_PRODUCER_BROKERS = cfg.getProperty("telemetry.kafka.producer.brokers");
        TELEMETRY_KAFKA_PRODUCER_TOPICS = cfg.getProperty("telemetry.kafka.producer.topics", ",");
        TELEMETRY_KAFKA_PRODUCER_GROUP_ID = cfg.getProperty("telemetry.kafka.producer.groupId");
        TELEMETRY_KAFKA_PRODUCER_ACKS = cfg.getProperty("telemetry.kafka.producer.acks");
        TELEMETRY_KAFKA_PRODUCER_RETRIES = Integer.parseInt(cfg.getProperty("telemetry.kafka.producer.retries"));
        TELEMETRY_KAFKA_PRODUCER_BATCH_SIZE = Integer.parseInt(cfg.getProperty("telemetry.kafka.producer.batch.size"));
        TELEMETRY_KAFKA_PRODUCER_LINGER_MS = Integer.parseInt(cfg.getProperty("telemetry.kafka.producer.linger.ms"));
        TELEMETRY_KAFKA_PRODUCER_BUFFER_MEMORY = Integer.parseInt(cfg.getProperty("telemetry.kafka.producer.buffer.memory"));
        TELEMETRY_KAFKA_PRODUCER_KEY_SERIALIZER = cfg.getProperty("telemetry.kafka.producer.key.serializer");
        TELEMETRY_KAFKA_PRODUCER_VALUE_SERIALIZER = cfg.getProperty("telemetry.kafka.producer.value.serializer");
        TELEMETRY_KAFKA_PRODUCER_KERBEROS_ENABLED = Boolean.parseBoolean(cfg.getProperty("telemetry.kafka.kerberos.enabled"));
        TELEMETRY_KAFKA_PRODUCER_SECURITY_PROTOCOL = cfg.getProperty("telemetry.kafka.security.protocol");
        TELEMETRY_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME = cfg.getProperty("telemetry.kafka.sasl.kerberos.service.name");



    }

}
