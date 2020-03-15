package com.subhadipmitra.code.module.producer.telemetry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.subhadipmitra.code.module.models.SRC_MapTelemetry;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.*;
import static com.subhadipmitra.code.module.init.Main.mapper;


public class TelemetryProducer {

    public static Producer<String, String> producer;
    private static final Logger logger = LoggerFactory.getLogger(TelemetryProducer.class);


    public TelemetryProducer() {
        Properties props = createProducerConfig();
        producer = new KafkaProducer<String, String>(props);
    }


    private static Properties createProducerConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", TELEMETRY_KAFKA_PRODUCER_BROKERS);
        props.put("acks", TELEMETRY_KAFKA_PRODUCER_ACKS);
        props.put("retries", TELEMETRY_KAFKA_PRODUCER_RETRIES);
        props.put("batch.size", TELEMETRY_KAFKA_PRODUCER_BATCH_SIZE);
        props.put("linger.ms", TELEMETRY_KAFKA_PRODUCER_LINGER_MS);
        props.put("buffer.memory", TELEMETRY_KAFKA_PRODUCER_BUFFER_MEMORY);
        props.put("key.serializer", TELEMETRY_KAFKA_PRODUCER_KEY_SERIALIZER);
        props.put("value.serializer", TELEMETRY_KAFKA_PRODUCER_VALUE_SERIALIZER);
        // Kerberos is enabled, get the required configs
        if(TELEMETRY_KAFKA_PRODUCER_KERBEROS_ENABLED){
            props.put("sasl.kerberos.service.name", TELEMETRY_KAFKA_PRODUCER_KERBEROS_SERVICE_NAME);
        }
            props.put("security.protocol", TELEMETRY_KAFKA_PRODUCER_SECURITY_PROTOCOL);

      return props;
    }

/**
 * Send Telemetry
 * @param batchId batch id
 * @param batchWindow batch window
 * @param srcName source name
 * @param srcId source id
 * @param recordsProcessed records processed
 * @param firstRecordId first records
 * @param landingTime landing time
 */
  public void sendTelemetry(String batchId, String batchWindow, String srcName,
                           String srcId, long recordsProcessed, String firstRecordId,
                           long landingTime){



      // get Avro telemetry record
      SRC_MapTelemetry telemetry_record = new SRC_MapTelemetry(
              batchId,
              batchWindow,
              srcName, srcId,
              recordsProcessed,
              firstRecordId,
              landingTime
      );

      String telemetry_recordJSON = null;
      try {
          telemetry_recordJSON = mapper.writeValueAsString(telemetry_record);
          // send
          producer.send(new ProducerRecord<String, String>(TELEMETRY_KAFKA_PRODUCER_TOPICS.get(0), batchId, telemetry_recordJSON), new Callback() {
              public void onCompletion(RecordMetadata metadata, Exception e) {
                  if (e != null) {
                      logger.error(e.toString());
                      e.printStackTrace();
                  }
                  logger.debug("TelemetryJSONRecord [offset="+ metadata.offset()+", batchId=" + batchId + ", topic=" + TELEMETRY_KAFKA_PRODUCER_TOPICS.get(0) +"]");
              }
          });
      } catch (JsonProcessingException e) {
          e.printStackTrace();
      }




  }



    /** Invocated by Shut Down Hook */
    public void shutdown(){
        logger.info("flushing and closing");
        producer.flush();
        producer.close();
    }
}
