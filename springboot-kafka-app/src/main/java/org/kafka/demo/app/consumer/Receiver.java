package org.kafka.demo.app.consumer;

import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kafka.demo.app.DBStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Receiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

  //@Autowired
  //private DBStore dbStore;

  private CountDownLatch latch = new CountDownLatch(1);

  public CountDownLatch getLatch() {
    return latch;
  }

  public long lastRecordTime = System.currentTimeMillis();

  @KafkaListener(topics = "${topic.boot}", containerFactory = "concurrentKafkaListenerContainerFactory")
  public void receive(ConsumerRecord<?, ?> consumerRecord) {

    if((System.currentTimeMillis()-lastRecordTime) > 1000 * 10){
      //dbStore.insertData(consumerRecord.value().toString(),false);
      LOGGER.info("received data='{}'", consumerRecord.toString());
      lastRecordTime = System.currentTimeMillis();
    } else {
      //dbStore.insertData(consumerRecord.value().toString(), false);
    }
    latch.countDown();
  }
}
