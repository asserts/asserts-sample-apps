/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


//@Component
@Slf4j
public class DBStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBStore.class);
    public DBStore(){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/postgres",
                            "user1", "user1");
            LOGGER.info("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS KAFKA_DATA " +
                    " (DATA          TEXT    NOT NULL) ";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
            try {
                c.close();
            }catch (Exception ex){
                LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
            }
        }
        System.out.println("Table created successfully");
    }

    public void insertData(String data, boolean isRollback) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/postgres",
                            "user1", "user1");
            c.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "INSERT INTO KAFKA_DATA (DATA) "
                    + "VALUES ('"+ data +"');";
            stmt.executeUpdate(sql);
            stmt.close();
            if(isRollback){
                //Thread.sleep(1000);
                c.rollback();
                System.out.println("Records rollback");
                LOGGER.info("Records rollback");
            } else {
                c.commit();
                //LOGGER.info("Records created successfully");
            }
            c.close();
        } catch (Exception e) {
            LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
            try {
                c.close();
            }catch (Exception ex){
                LOGGER.error( e.getClass().getName()+": "+ e.getMessage() );
            }
        }

    }
}
