/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.mysql.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
@Slf4j
public class SqlWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlWriter.class);
    private boolean isTableCreated = false;

    private void createTable(){
        if(isTableCreated) {
            return;
        }
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager
                    .getConnection("jdbc:mysql://db:3306/mysql",
                            "root", "example");
            LOGGER.info("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS SAMPLE_DATA " +
                    " (ID int NOT NULL AUTO_INCREMENT, " +
                    "DATA         TEXT    NOT NULL," +
                    "PRIMARY KEY (ID))" ;
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            isTableCreated = true;
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
    public void insertData(String data) {
        createTable();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager
                    .getConnection("jdbc:mysql://db:3306/mysql",
                            "root", "example");
            c.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "INSERT INTO SAMPLE_DATA (DATA) "
                    + "VALUES ('" + data + "')";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
            try {
                c.close();
            } catch (Exception ex) {
                LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}
