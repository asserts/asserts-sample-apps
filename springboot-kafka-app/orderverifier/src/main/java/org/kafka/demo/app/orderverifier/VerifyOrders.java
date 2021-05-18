/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.orderverifier;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

@Component
@Slf4j
public class VerifyOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyOrders.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void send(String topic, String key, String data) {
        kafkaTemplate.send(topic, key, data);
    }

    @KafkaListener(topics = "${topic.consume}", containerFactory = "concurrentKafkaListenerContainerFactory")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        //LOGGER.info("received data='{}'", consumerRecord.toString());
        send("verifiedorders", consumerRecord.key().toString(), consumerRecord.value().toString());
    }
}
