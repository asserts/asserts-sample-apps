/*
 *  Copyright © 2021.
 *  Asserts, Inc. - All Rights Reserved
 */

package org.kafka.demo.app;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @RequestMapping(
            path = "/ping",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }
}
