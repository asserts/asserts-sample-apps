package org.kafka.demo.app.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kafka.demo.app.DBStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

//@Component
@Slf4j
public class Receiver_Clear {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver_Clear.class);

    //@Autowired
    //private DBStore dbStore;

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }


    @KafkaListener(topics = "${topic.produce}", containerFactory = "concurrentKafkaListenerContainerFactoryClear")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        //dbStore.insertData(consumerRecord.value().toString(), true);
        //LOGGER.info("Clearing Topic "+ consumerRecord.value().toString());
        //System.out.println("Clearing Topic");
        latch.countDown();
    }
}
