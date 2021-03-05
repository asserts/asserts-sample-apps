package org.kafka.demo.app;

import org.kafka.demo.app.producer.Sender;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendAndReceive {

    private static String BOOT_TOPIC = "Topic3";

    @Autowired
    private Sender sender;

    //@Autowired
    //private Receiver receiver;

    //public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, BOOT_TOPIC);

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:1000}",
            initialDelayString = "${model.refresh.initialDelay:1000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run() {
        System.out.println("Sending the Msg!!");
        try {
            sender.send(BOOT_TOPIC, "Hello Boot!");

            //receiver.getLatch().await(1000, TimeUnit.MILLISECONDS);
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
}
