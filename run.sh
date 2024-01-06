#!/usr/bin/env bash

# SMPP Kafka Producer - Startup Script
# Usage: ./run.sh -p <port> [additional_ports...]

java -Xms64m -Xmx2048m \
    -Dconfig.properties=settings/config.properties \
    -Dconfig.smpp=settings/context.xml \
    -Dlog4j2.configurationFile=file:settings/log4j2.xml \
    -jar smpp-kafka-producer-2.0.1-spring-boot.jar "$@"
