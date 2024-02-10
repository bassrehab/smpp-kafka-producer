package io.smppgateway.producer.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.smppgateway.metrics.MetricsRegistry;
import io.smppgateway.producer.telemetry.TelemetryProducer;
import io.smppgateway.common.utilities.PeriodicUUID;
import io.smppgateway.common.utilities.UniqueId;
import io.smppgateway.models.SMS;
import io.smppgateway.models.SRC_MapSMPP;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import org.apache.kafka.clients.producer.*;

import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.*;
import static io.smppgateway.init.Main.mapper;
import static io.smppgateway.init.Main.utilities;

public class SMPPProducer {
  private static final Logger logger = LoggerFactory.getLogger(SMPPProducer.class);
  private final MetricsRegistry metrics = MetricsRegistry.getInstance();
  public Producer<String, String> producer;
  private static long recordsProcessed;


  public SMPPProducer() {

    // Get Kafka properties
    Properties props = createProducerConfig();
    producer = new KafkaProducer<>(props);
    recordsProcessed = 0L;

  }

  private Properties createProducerConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", SOURCE_SMPP_KAFKA_PRODUCER_BROKERS);
        props.put("acks", SOURCE_SMPP_KAFKA_PRODUCER_ACKS);
        props.put("retries", SOURCE_SMPP_KAFKA_PRODUCER_RETRIES);
        props.put("batch.size", SOURCE_SMPP_KAFKA_PRODUCER_BATCH_SIZE);
        props.put("linger.ms", SOURCE_SMPP_KAFKA_PRODUCER_LINGER_MS);
        props.put("buffer.memory", SOURCE_SMPP_KAFKA_PRODUCER_BUFFER_MEMORY);
        props.put("key.serializer", SOURCE_SMPP_KAFKA_PRODUCER_KEY_SERIALIZER);
        props.put("value.serializer", SOURCE_SMPP_KAFKA_PRODUCER_VALUE_SERIALIZER);

        // Kerberos is enabled, get the required configs
        if(SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_ENABLED){
          props.put("sasl.kerberos.service.name", SOURCE_SMPP_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME);
        }

        props.put("security.protocol", SOURCE_SMPP_KAFKA_PRODUCER_SECURITY_PROTOCOL);

      return props;
  }


    /**
     * Abstraction for sending kafka JSON message.
     * @param sms SMS Object
     *
     */
  public void sendMessage(SMS sms){


        String id            = new UniqueId().getUuid();
        boolean reset_window = PeriodicUUID.getInstance().is_reset;

        String topic         = SOURCE_SMPP_KAFKA_PRODUCER_TOPICS.get(0);
        String srcId         = topic;

        String batchId       = PeriodicUUID.getInstance().UUID;
        String isProcessed   = sms.getHas_parsing_errors(); // Link to parsing errors. If it has error, not further processing
        long entryTime       = Long.parseLong(utilities.getCurrentTime());

        // Create Source Object
        SRC_MapSMPP trigger = new SRC_MapSMPP(
                                      id,
                                      srcId,
                                      sms.getPayload(),
                                      entryTime,
                                      isProcessed,
                                      batchId,
                                      sms.getHas_parsing_errors(),
                                      sms.getMsisdn(),
                                      sms.getKeyword(),
                                      sms.getValue(),
                                      sms.getExtradata(),
                                      sms.getTimestamp() );

      try {
          String triggerJSON = mapper.writeValueAsString(trigger);

          // Start timing Kafka send
          Timer.Sample kafkaTimer = metrics.startKafkaTimer();

          // send
          producer.send(new ProducerRecord<>(topic, id, triggerJSON), (metadata, e) -> {
              // Record Kafka send timing
              metrics.recordKafkaSendTime(kafkaTimer);

              if (e != null) {
                  logger.error("Failed to send message to Kafka topic {}: {}", topic, e.getMessage(), e);
                  metrics.incrementKafkaFailed();
                  return;
              }

              // Update metrics
              metrics.incrementKafkaSent();
              recordsProcessed++;

              logger.debug("L0_SMPPJSONRecord ["
                      + " offset="+ metadata.offset()
                      + ", uuid=" + id
                      + ", topic=" + topic
                      + ", hasParsingErrors:"+ sms.getHas_parsing_errors()
                      + ", payload:"+ sms.getPayload()
                      + ", entryTime=" + entryTime
                      + ", isProcessed=" + isProcessed
                      + ", msisdn:"+ sms.getMsisdn()
                      + ", keyword:"+ sms.getKeyword()
                      + ", value:"+ sms.getValue()
                      + ", timestamp:"+ sms.getTimestamp()
                      +"]");
          });


      } catch (JsonProcessingException e) {
          logger.error("Failed to serialize SMS to JSON: {}", sms, e);
          metrics.incrementKafkaFailed();
      }


      // If the BatchID has been 'reset' send to Telemetry.
      if(reset_window){
          // Create the Telemetry producer instance.
          TelemetryProducer tp = new TelemetryProducer();

          String batchWindow = SOURCE_SMPP_SERVICE_BATCH_WINDOW;

          String srcName = TELEMETRY_KAFKA_PRODUCER_TOPICS.get(0);

          // Do your stuff.
          tp.sendTelemetry(batchId, batchWindow, srcName, srcId, recordsProcessed, id, entryTime);
          tp.shutdown();

          recordsProcessed = 0L; // reset records count

      }


  }


    /** Invocated by Shut Down Hook */
    public void shutdown(){
        logger.info("flushing and closing");
        this.producer.flush();
        this.producer.close();
    }

}
