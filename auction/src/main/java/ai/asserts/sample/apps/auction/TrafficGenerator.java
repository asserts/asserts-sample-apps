/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class to generate traffic to the sample auction application
 */
@SuppressWarnings({"rawtypes", "unused"})
@Component
@Slf4j
public class TrafficGenerator {

    public TrafficGenerator(@Value("${sample.app.role:server}") String role) {
        if ("test-client".equals(role)) {
            log.info("Setting up test client thread to initiate test requests");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.submit(this::init);
        }
    }

    @SuppressWarnings("BusyWait")
    public void init() {
        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        do {
            try {
                log.info("About to send workload request..");
                executorService.submit(() -> {
                    try {
                        List items = restTemplate.exchange("http://auction.app.dev.asserts.ai/items",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Item>>() {
                                }).getBody();
                        if (items != null) {
                            log.info("Got " + items.size() + " bids");
                        }
                    } catch (Exception e) {
                        log.error("Get Items error", e);
                    }
                }).get();

                executorService.submit(() -> {
                    try {
                        List bids = restTemplate.exchange("http://auction.app.dev.asserts.ai/bids",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Bid>>() {
                                }).getBody();
                        if (bids != null) {
                            log.info("Got " + bids.size() + " bids");
                        }
                    } catch (Exception e) {
                        log.error("Get Bids error", e);
                    }
                }).get();

                executorService.submit(() -> {
                    try {
                        String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/http/404",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<String>() {
                                }).getBody();
                        if (response != null) {
                            log.info(response);
                        }
                    } catch (Exception e) {
                        log.error("HTTP Status 404", e);
                    }
                }).get();

                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            restTemplate.exchange("http://auction.app.dev.asserts.ai/useCPU",
                                    HttpMethod.GET,
                                    null,
                                    new ParameterizedTypeReference<String>() {
                                    });
                            log.info("Trigger CPU Usage ");
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();

                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/useMemory",
                                    HttpMethod.GET,
                                    null,
                                    new ParameterizedTypeReference<String>() {
                                    }).getBody();
                            log.info(response);
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();


                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/readBidsFromDynamo",
                                    HttpMethod.GET,
                                    null,
                                    new ParameterizedTypeReference<String>() {
                                    }).getBody();
                            log.info(response);
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();

                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/writeBidsToDynamo",
                                    HttpMethod.POST,
                                    new HttpEntity<>("Some Body"),
                                    new ParameterizedTypeReference<String>() {
                                    }).getBody();
                            log.info(response);
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();

                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/readBidsFromS3",
                                    HttpMethod.GET,
                                    null,
                                    new ParameterizedTypeReference<String>() {
                                    }).getBody();
                            log.info(response);
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();

                executorService.submit(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            String response = restTemplate.exchange("http://auction.app.dev.asserts.ai/writeBidsToS3",
                                    HttpMethod.POST,
                                    new HttpEntity<>("Some Body"),
                                    new ParameterizedTypeReference<String>() {
                                    }).getBody();
                            log.info(response);
                        } catch (Exception e) {
                            log.error("Compression Error", e);
                        }
                    }
                }).get();

                Thread.sleep(15000);
            } catch (Exception e) {
                log.error("Thread error", e);
                break;
            }
        }
        while (true);
    }
}
