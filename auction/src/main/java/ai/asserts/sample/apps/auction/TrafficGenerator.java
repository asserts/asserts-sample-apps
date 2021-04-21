/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class to generate traffic to the sample auction application
 */
@SuppressWarnings({"rawtypes"})
public class TrafficGenerator {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        do {
            executorService.submit(() -> {
                try {
                    List items = restTemplate.exchange("http://sample-app-auction.app.dev.asserts.ai/items",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Item>>() {
                            }).getBody();
                    System.out.println("Got " + items.size() + " bids");
                } catch (Exception ignored) {
                }
            });

            executorService.submit(() -> {
                try {
                    List bids = restTemplate.exchange("http://sample-app-auction.app.dev.asserts.ai/bids",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Bid>>() {
                            }).getBody();
                    System.out.println("Got " + bids.size() + " bids");
                } catch (Exception ignored) {
                }
            });

            executorService.submit(() -> {
                try {
                    String response = restTemplate.exchange("http://sample-app-auction.app.dev.asserts.ai/status/404",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<String>() {
                            }).getBody();
                    System.out.println(response);
                } catch (Exception ignored) {
                }
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
        while (true);
    }
}
