package org.kafka.demo.app.producer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);


  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;


  public void send(String topic, String data) {
    //LOGGER.info("sending data='{}' to topic='{}'", data, topic);
    kafkaTemplate.send(topic, data);
  }
}
