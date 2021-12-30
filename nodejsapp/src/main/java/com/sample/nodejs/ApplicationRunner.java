/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package com.sample.nodejs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    @Autowired
    public ApplicationRunner() {}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("NodeJS Client App Started.");
    }
}
