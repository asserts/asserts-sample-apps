/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricPublisher implements Runnable {
    private final ScheduledExecutorService scheduledExecutorService;
    private final ScenarioSimulator scenarioSimulator;
    private final RestTemplate restTemplate;

    @SuppressWarnings("unused")
    public MetricPublisher(ScenarioSimulator scenarioSimulator, RestTemplate restTemplate) {
        this.scenarioSimulator = scenarioSimulator;
        this.restTemplate = restTemplate;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 15_000, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        StringWriter stringWriter = new StringWriter();
        CollectorRegistry collectorRegistry = scenarioSimulator.getCollectorRegistry();

        log.info("About to gather metrics");
        Enumeration<Collector.MetricFamilySamples> mfs = collectorRegistry.metricFamilySamples();
        try {
            TextFormat.write004(stringWriter, mfs);
        } catch (IOException e) {
            log.error("Failed to extract metrics", e);
        }

        String payload = stringWriter.getBuffer().toString();
        log.info("Gathered metrics {}", payload);
        String url = "https://chief.tsdb.dev.asserts.ai/api/v1/import/prometheus";
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url,
                    HttpMethod.POST,
                    createRequestEntity(payload),
                    new ParameterizedTypeReference<String>() {
                    });
            log.info("POST to {} return {}", url, responseEntity);
        } catch (RestClientException e) {
            log.error("Failed to send payload to url:" + url, e);
        }
    }

    private HttpEntity<?> createRequestEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("chief", "chieftenant");
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentLength(body.length());
        return new HttpEntity<>(body, headers);
    }
}
