/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.prometheus.client.Collector.Type.GAUGE;

@Getter
public abstract class BaseSimulator {
    protected final String name;
    protected final Integer timeoutSeconds;
    protected final Integer memoryMb;
    protected final Service callsService;
    protected int step = 0;
    protected final int maxStep;
    protected final int defaultInvocations = 20;
    protected final double defaultMemoryUtilization = 50.0D;
    protected final double defaultFnExecutionsAvg = 0.5D;
    protected final double defaultRegionalExecutionsAvg = 30;
    protected final double defaultLatencyAvgMs = 500;
    protected final double defaultLatencyP99Ms = 750;

    public final Map<String, String> regionLabels = new ImmutableMap.Builder<String, String>()
            .put("region", "us-west-2")
            .put("cw_namespace", "AWS/Lambda")
            .put("namespace", "AWS/Lambda")
            .put("container", "aws-exporter")
            .put("service", "aws-exporter")
            .put("endpoint", "aws")
            .put("instance", "0.8.92.5:8010")
            .put("pod", "aws-exporter-7cbf9fbbd6-b2v6x")
            .putAll(ScenarioSimulator.TENANT_ENV_LABELS)
            .build();


    public BaseSimulator(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService,
                         int maxStep) {
        this.name = name;
        this.timeoutSeconds = timeoutSeconds;
        this.memoryMb = memoryMb;
        this.callsService = callsService;
        this.maxStep = maxStep;
    }

    public boolean isDone() {
        if (step == maxStep) {
            reset();
            return true;
        }
        return false;
    }

    public abstract List<Collector.MetricFamilySamples> emitMetrics();

    public abstract void reset();

    public List<Collector.MetricFamilySamples> emitMetrics(double memoryUtilization,
                                                           int invocations, int errors, int throttles,
                                                           double latencyAvg, double latencyP99,
                                                           double fnExecutionsAvg, double regionExecutionsAvg) {
        List<Collector.MetricFamilySamples> familySamples = new ArrayList<>();
        Map<String, String> functionLabels = new TreeMap<>(regionLabels);

        functionLabels.put("job", name);
        functionLabels.put("d_function_name", name);

        Map<String, String> versionedFnLabels = new TreeMap<>(functionLabels);
        versionedFnLabels.put("d_executed_version", "1");

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_memory_limit_mb", memoryMb));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_timeout_seconds", timeoutSeconds));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_memory_utilization_avg", memoryUtilization));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_memory_utilization_avg", memoryUtilization));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_cpu_total_time_sum", 0.2 * invocations * latencyAvg));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_cpu_total_time_sum", 0.2 * invocations * latencyAvg));

        double random = Math.random();
        int successfulInvocations = invocations - errors;
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_tx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_rx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));

        familySamples.add(buildFamily(functionLabels, "aws_lambda_tx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_rx_bytes_sum", successfulInvocations * (1024.0D * 8 + 4 * random * 1024.0D)));

        int scrape_interval = 60;
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_invocations_sum", invocations * scrape_interval));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_invocations_sum", invocations * scrape_interval));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_errors_sum", errors * scrape_interval));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_errors_sum", errors * scrape_interval));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_throttles_sum", throttles * scrape_interval));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_throttles_sum", throttles * scrape_interval));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_duration_avg", latencyAvg));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_duration_avg", latencyAvg));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_duration_p99", latencyP99));
        familySamples.add(buildFamily(functionLabels, "aws_lambda_duration_p99", latencyP99));

        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_allocated_concurrency", 2.0D));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_reserved_concurrency", 5.0D));
        familySamples.add(buildFamily(versionedFnLabels, "aws_lambda_concurrent_executions_avg", fnExecutionsAvg));

        familySamples.add(buildFamily(functionLabels, "aws_lambda_concurrent_executions_avg", fnExecutionsAvg));
        familySamples.add(buildFamily(regionLabels, "aws_lambda_concurrent_executions_avg", regionExecutionsAvg));
        familySamples.add(buildFamily(regionLabels, "aws_lambda_account_limit", 100.0D));

        familySamples.addAll(callsService.getUpAndLatencyMetric((latencyAvg - 250.0D) / 1000));

        Map<String, String> callsRelationLabels = new ImmutableMap.Builder<String, String>()
                .putAll(ScenarioSimulator.TENANT_ENV_LABELS)
                .put("job", name)
                .put("namespace", "AWS/Lambda")
                .put("dst_job", callsService.getName())
                .put("dst_namespace", callsService.getNamespace())
                .build();
        familySamples.add(buildFamily(callsRelationLabels, "asserts:relation:calls", 1.0D));
        return familySamples;
    }

    private Collector.MetricFamilySamples buildFamily(Map<String, String> functionLabels, String metricName, double metricValue) {
        return new Collector.MetricFamilySamples(metricName, GAUGE, "",
                ImmutableList.of(new Collector.MetricFamilySamples.Sample(metricName,
                        new ArrayList<>(functionLabels.keySet()),
                        new ArrayList<>(functionLabels.values()),
                        metricValue)));
    }
}
