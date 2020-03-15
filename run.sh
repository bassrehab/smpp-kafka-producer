#!/usr/bin/env bash
java -Xms32m -Xmx1024m \
    -Dconfig.properties=settings/config.properties \
    -Dconfig.smpp=settings/context.xml \
    -Dlog4j.configuration=file:settings/log4j.properties \
    -jar module-smpp-0.2.0-spring-boot.jar -p 23940

