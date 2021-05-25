/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.ordercategorizer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CategorizeOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategorizeOrders.class);

    private static String TOPIC_LOCAL = "localorders";
    private static String TOPIC_OUTSTATION = "outstationorders";
    private boolean sendToLocal = true;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void send(String topic, String key, String data) {
        kafkaTemplate.send(topic, key, data);
    }

    @KafkaListener(topics = "${topic.consume}", containerFactory = "concurrentKafkaListenerContainerFactory")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        //LOGGER.info("received data='{}'", consumerRecord.toString());
        if(sendToLocal) {
            send(TOPIC_LOCAL, consumerRecord.key().toString(), consumerRecord.value().toString());
            //LOGGER.info("sending to local data='{}'", consumerRecord.toString());
        } else {
            send(TOPIC_OUTSTATION, consumerRecord.key().toString(), consumerRecord.value().toString());
            //LOGGER.info("sending to outstation data='{}'", consumerRecord.toString());
        }
        sendToLocal = !sendToLocal;
    }

}
