/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package org.kafka.demo.app.ordercategorizer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.kafka.demo.app.ordercategorizer"})
@Slf4j
public class OrderCategorizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderCategorizerApplication.class, args);
    }
}