/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

//@Component
@Slf4j
public class DBStore_Parallel {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBStore_Parallel.class);

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:60000}",
            initialDelayString = "${model.refresh.initialDelay:5000}")
    public void connectToDB() {

        try {
            Thread t1 = new Thread(new DBThread("c1"));
            t1.start();
            Thread t2 = new Thread(new DBThread("c2"));
            t2.start();
            //Thread t3 = new Thread(new DBThread("c3"));
            //t3.start();

        } catch ( Exception e ) {
            LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected successfully");
    }
    private class DBThread implements Runnable {

        private  final String threadName;
        public DBThread(String name) {
            this.threadName = name;
        }
        @Override
        public void run() {
            Connection c1=null;
            try {
                Class.forName("org.postgresql.Driver");
                c1 = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5433/postgres",
                                "user1", "user1");
                System.out.println("Opened database successfully for " + threadName);
                Thread.sleep(16000);
                System.out.println("Closed database successfully for " + threadName);
                c1.close();
            }catch (Exception e){
                LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
                try {
                    c1.close();
                }catch (Exception ex){
                    LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
                }
            }

        }
    }
}
