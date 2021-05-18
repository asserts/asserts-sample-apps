package org.kafka.demo.app.ordercategorizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderCategorizerTask {

    @Autowired
    CategorizeOrders categorizeOrders;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:5000}",
            initialDelayString = "${model.refresh.initialDelay:10000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run() {
        categorizeOrders.start();
    }
}