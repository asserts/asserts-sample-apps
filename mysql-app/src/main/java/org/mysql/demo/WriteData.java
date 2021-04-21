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
public class WriteData {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteData.class);

    @Autowired
    private SqlWriter sqlWriter;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:1000}",
            initialDelayString = "${model.refresh.initialDelay:5000}")
    @Timed(description = "Time spent ", histogram = true)
    public void run(){

        LOGGER.info("Writing the Data to DB!!");
        try {
            sqlWriter.insertData("Hello Boot Parse My Data");
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
