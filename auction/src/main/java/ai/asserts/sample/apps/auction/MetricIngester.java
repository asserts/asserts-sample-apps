/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;

@Slf4j
@AllArgsConstructor
@SuppressWarnings("unused")
public class MetricIngester {
    private final RestTemplate restTemplate;
    private final String timeoutDuration;
    private final String username;
    private final String password;
    private final String url;
    @Setter
    private boolean dryRun;


    public void sendMetrics(String metricPayload) {
        try {
            if (dryRun) {
                System.out.println(metricPayload);
            } else {
                ResponseEntity<Void> response = restTemplate.exchange(url,
                        HttpMethod.POST,
                        createRequestEntity(username, password, metricPayload),
                        new ParameterizedTypeReference<Void>() {
                        },
                        timeoutDuration);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn("Request failed with status code: {}", response.getStatusCode());
                }
            }
        } catch (HttpClientErrorException e) {
            log.warn("Request reload with status code: {}", e.getStatusCode(), e);
        }
    }

    private HttpEntity<?> createRequestEntity(String username, String password, String params) {
        HttpHeaders headers = null;
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
        }
        return new HttpEntity<>(params, headers);
    }

    @With
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MetricRequest {
        private String query;
        private Long initialValue;
        private Long stepBy;
    }

    public static void main(String[] args) {
        String URL = "https://chief.tsdb.dev.asserts.ai/api/v1/import/prometheus";
        RestTemplate restTemplate = new RestTemplate();
        MetricIngester metricIngestion = new MetricIngester(
                restTemplate, "30s", "chief", "chieftenant", URL, false
        );
        LambdaSimulator lambdaSimulator = new LambdaSimulator("CheckoutService", 5, 128,
                new Service("DiscountService", "promotion-services"),
                metricIngestion);
        lambdaSimulator.keepEmittingMetrics();
    }

    @AllArgsConstructor
    public static class LambdaSimulator {
        public final static String regionLabels = "asserts_tenant=\"chief\", asserts_site=\"us-west-2\", region=\"us-west-2\", cw_namespace=\"AWS/Lambda\", namespace=\"AWS/lambda\"";
        public final static String exporterLabels = "container=\"aws-exporter\", service=\"aws-exporter\", pod=\"aws-exporter-7cbf9fbbd6-b2v6x\", endpoint=\"aws\", instance=\"0.8.92.5:8010\"";
        public final static String envLabels = "asserts_env=\"dev\", tag_asserts_env_name=\"dev\"";
        public final static String commonLabels = regionLabels + ", " + exporterLabels + ", " + envLabels + ", ";

        public final static String timeoutSecondsMetric = "aws_lambda_timeout_seconds{%s" +
                "d_function_name=\"%s\", job=\"%s\"" +
                "} %d\n";
        public final static String memoryLimitMbMetric = "aws_lambda_memory_limit_mb{%s" +
                "d_function_name=\"%s\", job=\"%s\"" +
                "} %d\n";
        public static final String memoryUtilizationAvgMetric = "aws_lambda_memory_utilization_avg{%s" +
                "d_function_name=\"%s\", job=\"%s\"} %f\n";


        public final static String invocationsSumMetric = "aws_lambda_invocations_sum{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %d\n";

        public final static String errorsSumMetric = "aws_lambda_errors_sum{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %d\n";

        public final static String throttlesSumMetric = "aws_lambda_throttles_sum{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %d\n";

        public final static String durationMillisAvgMetric = "aws_lambda_duration_avg{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %f\n";
        public final static String durationMillisP99Metric = "aws_lambda_duration_p99{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %f\n";
        public final static String functionConcurrentExecutionsMetric = "aws_lambda_concurrent_executions_avg{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %f\n";
        public final static String functionAllocatedConcurrency = "aws_lambda_allocated_concurrency{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %d\n";
        public final static String functionReservedConcurrency = "aws_lambda_reserved_concurrency{%s" +
                "d_function_name=\"%s\", job=\"%s\" %s" +
                "} %d\n";
        public final static String regionConcurrentExecutionsMetric = "aws_lambda_concurrent_executions_avg{" +
                regionLabels + ", " + exporterLabels
                + "} %f\n";
        public final static String accountLimitMetric = "aws_lambda_accountLimit{" +
                regionLabels + ", " + exporterLabels
                + "} %d\n";

        private final static String callsRelation = "asserts:relation:calls{" +
                "asserts_tenant=\"chief\", asserts_env=\"dev\", asserts_site=\"us-west-2\", " +
                "namespace=\"AWS/Lambda\", job=\"%s\", tenant=\"chief\", " +
                "dst_job=\"%s\", dst_namespace=\"%s\"" +
                "} 1\n";

        private final String name;
        private final int timeoutSeconds;
        private final int memoryMb;
        private final Service callsService;
        private final MetricIngester metricIngestion;

        public void keepEmittingMetrics() {
            boolean condition = true;
            do {
                try {
//                    emitNormal();
//                    emitMemorySaturation();
//                    emitNormal();
//                    emitLatencyAvgIncrease();
//                    emitNormal();
//                    emitErrors();
//                    emitNormal();
                    emitFunctionThrottles();
//                    emitNormal();
//                    emitRegionalConcurrencySaturation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    condition = false;
                }
            } while (condition);
        }

        public void emitMemorySaturation() throws InterruptedException {
            // Start at 50% and go to 97% in 10 minutes, hover for 10 minutes and then ramp down to 50% in 10 minutes
            // Emit metrics every second
            double delta = 47.0D / 40;
            double memoryUtilization = 50.0D;
            for (int i = 0; i < 40; i++) {
                memoryUtilization += delta;
                double random = Math.random();
                emitMetrics(memoryUtilization,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                emitMetrics(memoryUtilization,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 40; i++) {
                memoryUtilization -= delta;
                double random = Math.random();
                emitMetrics(memoryUtilization,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }
        }

        public void emitLatencyAvgIncrease() throws InterruptedException {
            // Start at 50% and go to 97% in 10 minutes, hover for 10 minutes and then ramp down to 50% in 10 minutes
            // Emit metrics every second
            double delta = 4000.0 / 40;
            double latencyAvg = 500.0D;
            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                latencyAvg += delta;
                emitMetrics(50.0D,
                        30, 0, 0,
                        latencyAvg + random, latencyAvg + 250.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 20; i++) {
                double random = Math.random();
                emitMetrics(50.0D,
                        30, 0, 0,
                        latencyAvg + random, latencyAvg + 250.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                latencyAvg -= delta;
                emitMetrics(50.0D,
                        30, 0, 0,
                        latencyAvg + random, latencyAvg + 250.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }
        }

        public void emitErrors() throws InterruptedException {
            // Start at 0% and go to 30% in 30 minutes, hover for 10 minutes and then ramp down to 0% in 30 minutes
            // Emit metrics every second
            double delta = 30.0D / 120;
            double errorRate = 0.0D;
            int numInvocations = 30;
            for (int i = 0; i < 120; i++) {
                errorRate += delta;
                double random = Math.random();
                emitMetrics(50.0D + random,
                        numInvocations, (int) Math.ceil(numInvocations * errorRate * 0.01), 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                emitMetrics(50.0D + random,
                        numInvocations, (int) Math.ceil(numInvocations * errorRate * 0.01), 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 120; i++) {
                double random = Math.random();
                emitMetrics(50.0D + random,
                        numInvocations, (int) Math.ceil(numInvocations * errorRate * 0.01), 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }
        }

        public void emitFunctionThrottles() throws InterruptedException {
            // Make invocation rate 5x in 10 minutes
            // Start Throttling at 10 minutes
            // Let throttling continue till 15 minutes
            // Ramp down to normal invocation in 10 minutes
            // Emit metrics every second
            int delta = 80 / 40;
            int invocations = 20;
            double concurrencyDelta = 0.1D;
            double concurrency = 1.0D;
            for (int i = 0; i < 40; i++) {
                invocations += delta;
                concurrency += concurrencyDelta;
                double random = Math.random();
                emitMetrics(50.0D + random,
                        invocations, 0, 0,
                        500.0D + random, 750.0D + random,
                        concurrency, 30.0D + concurrency);
                Thread.sleep(15000L);
            }

            int throttleThreshold = invocations;
            for (int i = 0; i < 20; i++) {
                invocations += delta;
                double random = Math.random();
                emitMetrics(50.0D + random,
                        invocations, 0, invocations - throttleThreshold,
                        500.0D + random, 750.0D + random,
                        concurrency, 30.0D + concurrency);
                Thread.sleep(15000L);
            }

            for (int i = 0; i < 60; i++) {
                double random = Math.random();
                invocations -= delta;
                if (invocations < throttleThreshold) {
                    concurrency -= concurrencyDelta;
                }
                emitMetrics(50.0D + random,
                        invocations, 0, invocations > throttleThreshold ? invocations - throttleThreshold : 0,
                        500.0D + random, 750.0D + random,
                        concurrency, 30.0D + concurrency);
                Thread.sleep(15000L);
            }
        }

        public void emitRegionalConcurrencySaturation() throws InterruptedException {
            double regionalConcurrency = 30.0D;
            double delta = 65.0D / 40;

            // Ramp up
            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                regionalConcurrency += delta;
                emitMetrics(50.0D + 5 * random,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, regionalConcurrency + random);
                Thread.sleep(15000L);
            }

            // Sustain Peak
            for (int i = 0; i < 28; i++) {
                double random = Math.random();
                emitMetrics(50.0D + 5 * random,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, regionalConcurrency + random);
                Thread.sleep(15000L);
            }

            // Ramp down
            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                regionalConcurrency -= delta;
                emitMetrics(50.0D + 5 * random,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }
        }

        public void emitNormal() throws InterruptedException {
            for (int i = 0; i < 40; i++) {
                double random = Math.random();
                emitMetrics(50.0D + 5 * random,
                        30, 0, 0,
                        500.0D + random, 750.0D + random,
                        1.0D + random, 30.0D + random);
                Thread.sleep(15000L);
            }
        }

        private void emitMetrics(double memoryUtilization,
                                 int invocations, int errors, int throttles,
                                 double latencyAvg, double latencyP99,
                                 double fnExecutionsAvg, double regionExecutionsAvg) {
            String builder = format(memoryLimitMbMetric, commonLabels, name, name, memoryMb) +
                    format(timeoutSecondsMetric, commonLabels, name, name, timeoutSeconds) +
                    format(memoryUtilizationAvgMetric, commonLabels, name, name, memoryUtilization) +
                    format(invocationsSumMetric, commonLabels, name, name, "d_executed_version=\"1\"", invocations) +
                    format(errorsSumMetric, commonLabels, name, name, "d_executed_version=\"1\"", errors) +
                    format(throttlesSumMetric, commonLabels, name, name, "d_executed_version=\"1\"", throttles) +
                    format(durationMillisAvgMetric, commonLabels, name, name, "d_executed_version=\"1\"", latencyAvg) +
                    format(durationMillisP99Metric, commonLabels, name, name, "d_executed_version=\"1\"", latencyP99) +
                    format(functionAllocatedConcurrency, commonLabels, name, name, "d_executed_version=\"1\"", 2) +
                    format(functionReservedConcurrency, commonLabels, name, name, "d_executed_version=\"1\"", 5) +
                    format(functionConcurrentExecutionsMetric, commonLabels, name, name, "d_executed_version=\"1\"", fnExecutionsAvg) +
                    format(regionConcurrentExecutionsMetric, regionExecutionsAvg) +
                    format(accountLimitMetric, 100) +
                    callsService.getUpAndLatencyMetric(latencyAvg/1000 * 0.8) +
                    format(callsRelation, name, callsService.name, callsService.namespace);
            metricIngestion.sendMetrics(builder);
        }

    }

    @AllArgsConstructor
    public static class Service {
        private final String name;
        private final String namespace;
        private final static String upMetric = "up{asserts_tenant=\"chief\", asserts_env=\"dev\", asserts_site=\"us-west-2\"," +
                "job=\"%s\", container=\"%s\", namespace=\"%s\"} 1\n";

        public final static String latencyAvgMetric = "asserts:latency:average{asserts_tenant=\"chief\",asserts_env=\"dev\"," +
                "asserts_site=\"us-west-2\", container=\"%s\"," +
                "asserts_entity_type=\"Service\",asserts_request_type=\"inbound\"," +
                "asserts_source=\"springboot\", job=\"%s\", namespace=\"%s\"," +
                "tenant=\"chief\"} %f\n";

        String getUpAndLatencyMetric(double latencyValue) {
            return format(upMetric, name, name, namespace) +
                    format(latencyAvgMetric, name, name, namespace, latencyValue);
        }
    }
}
