package org.kafka.demo.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements ApplicationRunner {
    @Autowired
    public AppRunner() {}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Kafka Send Receive started");
    }
}
