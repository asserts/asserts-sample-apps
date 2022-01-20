/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Collector;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Builder
@Getter
public class Function extends MetricSource {
    private Tenant tenant;
    private Region region;
    private String name;
    @Builder.Default
    private String version = "1";
    @Builder.Default
    private int timeoutSeconds = 5;
    @Builder.Default
    private int memoryLimitMb = 128;

    private SQSQueue inputQueue;
    private List<SQSQueue> outputQueues;
    private List<Service> callsServices;

    List<Collector.MetricFamilySamples> getMetrics(int scrape_interval,
                                                   double memoryUtilization,
                                                   int invocations,
                                                   int errors,
                                                   int throttles,
                                                   double latencyAvg,
                                                   double latencyP99,
                                                   double fnExecutionsAvg) {
        double random = Math.random();
        List<Collector.MetricFamilySamples> familySamples = new ArrayList<>();

        Map<String, String> functionLabels = new TreeMap<>(region.labels());
        functionLabels.putAll(tenant.labels());

        functionLabels.put("job", name);
        functionLabels.put("d_function_name", name);

        Map<String, String> versionedFnLabels = new TreeMap<>(functionLabels);
        versionedFnLabels.put("d_executed_version", "1");

        Map<String, String> versionedWithRequestContext = new TreeMap<>(versionedFnLabels);
        Map<String, String> withRequestContext = new TreeMap<>(functionLabels);

        versionedWithRequestContext.put("asserts_request_context", "handler");
        withRequestContext.put("asserts_request_context", "handler");

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_memory_limit_mb", memoryLimitMb));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_timeout_seconds", timeoutSeconds));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_memory_utilization_avg", memoryUtilization));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_memory_utilization_avg", memoryUtilization));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_cpu_total_time_sum", 0.2 * invocations * latencyAvg));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_cpu_total_time_sum", 0.2 * invocations * latencyAvg));

        int successfulInvocations = invocations - errors;
        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_tx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));
        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_rx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));

        familySamples.add(buildFamily(withRequestContext, "aws_lambda_tx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));
        familySamples.add(buildFamily(withRequestContext, "aws_lambda_rx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));


        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_invocations_sum", invocations * scrape_interval));
        familySamples.add(buildFamily(withRequestContext, "aws_lambda_invocations_sum", invocations * scrape_interval));

        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_errors_sum", errors * scrape_interval));
        familySamples.add(buildFamily(withRequestContext, "aws_lambda_errors_sum", errors * scrape_interval));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_throttles_sum", throttles * scrape_interval));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_throttles_sum", throttles * scrape_interval));

        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_duration_avg", latencyAvg));
        familySamples.add(buildFamily(withRequestContext, "aws_lambda_duration_avg", latencyAvg));

        familySamples.add(buildFamily(versionedWithRequestContext, "aws_lambda_duration_p99", latencyP99));
        familySamples.add(buildFamily(withRequestContext, "aws_lambda_duration_p99", latencyP99));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_allocated_concurrency", 2.0D));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_reserved_concurrency", 5.0D));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_concurrent_executions_avg", fnExecutionsAvg));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_concurrent_executions_avg", fnExecutionsAvg));

        if (inputQueue != null) {
            familySamples.addAll(inputQueue.getMetrics(successfulInvocations));
            Map<String, String> labels = new TreeMap<>(tenant.labels());
            labels.putAll(region.labels());
            labels.put("event_source_name", inputQueue.getName());
            labels.put("event_source_type", "SQSQueue");
            labels.put("lambda_function", name);
            familySamples.add(buildFamily(labels, "aws_lambda_event_source", 1.0D));
        }

        if (!CollectionUtils.isEmpty(outputQueues)) {
            outputQueues.forEach(sqsQueue -> {
                familySamples.addAll(sqsQueue.getMetrics(successfulInvocations));

                Map<String, String> labels = new TreeMap<>(tenant.labels());
                labels.putAll(region.labels());
                labels.put("d_destination_name", sqsQueue.getName());
                labels.put("d_destination_type", "SQSQueue");
                labels.put("d_function_name", name);
                familySamples.add(buildFamily(labels, "aws_lambda_logs", 1.0D));
            });
        }

        if (!CollectionUtils.isEmpty(callsServices)) {
            callsServices.forEach(callsService -> {
                double outboundLatency = (latencyAvg - 250.0D) / 1000;
                familySamples.addAll(callsService.getUpAndLatencyMetric(invocations, outboundLatency));

                Map<String, String> callsRelationLabels = new ImmutableMap.Builder<String, String>()
                        .putAll(tenant.labels())
                        .putAll(region.labels())
                        .put("lambda_name", name)
                        .put("target_service", callsService.getName())
                        .put("target_api", "applyDiscounts")
                        .build();
                familySamples.add(buildFamily(callsRelationLabels, "lambda_outbound_calls", outboundLatency));
            });
        }

        return familySamples;
    }
}
