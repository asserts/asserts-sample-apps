package org.kafka.demo.app;

import org.kafka.demo.app.consumer.Receiver;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ReceiveMsg {
    private static String BOOT_TOPIC = "Topic2";

    @Autowired
    private Receiver receiver;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:5000}",
            initialDelayString = "${model.refresh.initialDelay:1000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run() {
        System.out.println("Receiving the Msg!!");
        try {
            receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
}
