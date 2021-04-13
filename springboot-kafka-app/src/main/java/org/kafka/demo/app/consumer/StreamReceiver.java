/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Slf4j
public class StreamReceiver {

    private static String STREAM_CONSUME = "Topic3";
    private static String STREAM_PRODUCE = "Topic5";

    public StreamReceiver(){
        createStream();
    }
    private void createStream(){
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(
                StreamsConfig.APPLICATION_ID_CONFIG,
                "wordcount");
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
        KStream<String, String> textLines = builder.stream(STREAM_CONSUME);
        textLines.selectKey((key, value) -> value.split(" ")[0])
                .groupByKey()
                //.reduce((v1,v2) -> v1+v2, Materialized.as( "1"))
                .count()
                .filter((s,l) -> l > 0)
                .toStream().to(STREAM_PRODUCE);
                //.toStream().foreach((w,c) ->System.out.println("word: " + w + " -> " + c));
        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.start();
    }
}
