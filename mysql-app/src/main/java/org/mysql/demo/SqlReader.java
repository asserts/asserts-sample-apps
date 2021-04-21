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
public class SqlReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlReader.class);

    public void readData() {
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
            String sql = "SELECT * FROM SAMPLE_DATA  "
                    + "WHERE ID > 0";
            stmt.execute(sql);
            stmt.close();
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
