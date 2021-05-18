/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.orderplacement;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlaceOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaceOrders.class);


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void send(String topic, String key, String data) {
        kafkaTemplate.send(topic, key,data);
    }

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:1000}",
            initialDelayString = "${model.refresh.initialDelay:5000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run() {
        LOGGER.info("Sending the Msg!!");
        try {
            for( int i=0; i < 10; i++) {
                send("orders", String.valueOf(i), "Hello Boot Parse My Data"+i);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
}
