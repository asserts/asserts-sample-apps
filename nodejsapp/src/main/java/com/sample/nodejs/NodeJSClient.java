/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package com.sample.nodejs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class NodeJSClient {

    @Autowired
    private Environment env;

    @Scheduled(fixedDelayString = "${model.refresh.fixedDelay:5000}",
            initialDelayString = "${model.refresh.initialDelay:5000}")
    public void run() throws Exception {
        get(env.getProperty("app.uri"));
    }

    public void get(String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response :" + response.body());
    }

}
