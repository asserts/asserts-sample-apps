/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package com.sample.nodejs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.sample.nodejs"})
public class NodeJSClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeJSClientApplication.class, args);
    }
}
