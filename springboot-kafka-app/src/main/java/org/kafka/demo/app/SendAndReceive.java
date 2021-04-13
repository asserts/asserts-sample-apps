package org.kafka.demo.app;

import org.kafka.demo.app.producer.Sender;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendAndReceive {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendAndReceive.class);
    private static String BOOT_TOPIC = "Topic3";

    @Autowired
    private Sender sender;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:1000}",
            initialDelayString = "${model.refresh.initialDelay:5000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run() {
        LOGGER.info("Sending the Msg!!");
        try {
            for( int i=0; i < 1000; i++) {
                sender.send(BOOT_TOPIC, String.valueOf(i), "Hello Boot Parse My Data"+i);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
}
