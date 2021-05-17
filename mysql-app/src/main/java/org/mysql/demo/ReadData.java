/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.mysql.demo;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReadData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadData.class);

    @Autowired
    private SqlReader sqlReader;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:10000}",
            initialDelayString = "${model.refresh.initialDelay:10000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run(){

        LOGGER.info("Reading the Data from DB!!");
        try {
            sqlReader.readData();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
