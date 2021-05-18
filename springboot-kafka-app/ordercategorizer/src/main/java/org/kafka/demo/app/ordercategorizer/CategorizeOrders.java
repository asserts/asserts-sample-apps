/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.ordercategorizer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import java.util.Properties;

@Component
@Slf4j
public class CategorizeOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategorizeOrders.class);

    private static String TOPIC_CONSUME = "verifiedorders";
    private static String TOPIC_LOCAL = "localorders";
    private static String TOPIC_OUTSTATION = "outstationorders";
    private static boolean stream_Started = false;
    private boolean isStream_Started = false;

    public void start(){
        if(isStream_Started) {
            return;
        }
        createLocalStream();
        isStream_Started = true;
    }
    public void createLocalStream(){
        LOGGER.info("Starting stream for Local Order ");
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(
                StreamsConfig.APPLICATION_ID_CONFIG,
                "localorders");
        String bootstrapServers = "kafka:9092";
        streamsConfiguration.put(
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        streamsConfiguration.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        streamsConfiguration.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        streamsConfiguration.put(
                StreamsConfig.STATE_DIR_CONFIG,
                System.getProperty("java.io.tmpdir"));
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(TOPIC_CONSUME);
        textLines.selectKey((key, value) -> value.split(" ")[0])
                .groupByKey()
                .count()
                .toStream().to(TOPIC_LOCAL);
        textLines.selectKey((key, value) -> value.split(" ")[0])
                .groupByKey()
                .count()
                .filter((s,l) -> l > 0)
                .toStream().to(TOPIC_OUTSTATION);
        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.start();
    }
}
