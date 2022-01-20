/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static io.prometheus.client.Collector.Type.GAUGE;

@Getter
public class Service extends MetricSource {
    private final Tenant tenant;
    private final String name;
    private final String namespace;
    private double latencyTotal;
    private long latencyCount;

    public Service(Tenant tenant, String name, String namespace) {
        this.tenant = tenant;
        this.name = name;
        this.namespace = namespace;
    }

    public List<Collector.MetricFamilySamples> getUpAndLatencyMetric(int invocationCount, double latencyValue) {
        List<Collector.MetricFamilySamples> metricFamilySamples = new ArrayList<>();
        SortedMap<String, String> labels = new TreeMap<>();
        labels.putAll(tenant.labels());
        labels.put("asserts_site", "us-west-2");
        labels.put("asserts_env", "lambda");
        labels.put("job", name);
        labels.put("container", name);
        labels.put("namespace", namespace);
        labels.put("service", name);
        labels.put("instance", "10.20.30.40:9090");
        labels.put("pod", "DiscountService-pod-123");
        labels.put("workload", name);
        labels.put("asserts_source", "springboot");

        metricFamilySamples.add(new Collector.MetricFamilySamples("up", GAUGE, "",
                Collections.singletonList(new Collector.MetricFamilySamples.Sample(
                        "up",
                        new ArrayList<>(labels.keySet()),
                        new ArrayList<>(labels.values()),
                        1.0D))));

        SortedMap<String, String> copy = new TreeMap<>(labels);
        copy.remove("job");
        copy.remove("instance");
        copy.put("workload_type", "ReplicaSet");
        String metricName = "asserts:mixin_pod_workload";
        metricFamilySamples.add(metricSample(GAUGE, copy, metricName, 1.0D));

        labels.put("asserts_entity_type", "ServiceInstance");
        labels.put("asserts_request_type", "inbound");
        labels.put("asserts_source", "springboot");
        labels.put("asserts_request_context", "/applyDiscounts");

        metricFamilySamples.add(metricSample(GAUGE, labels, "asserts:latency:count",
                latencyCount += invocationCount));

        metricFamilySamples.add(metricSample(GAUGE, labels, "asserts:latency:total",
                latencyTotal += invocationCount * latencyValue));

        labels.remove("instance");
        labels.put("asserts_entity_type", "Service");
        metricFamilySamples.add(metricSample(GAUGE, labels, "asserts:latency:p99",
                latencyValue / 0.8));
        return metricFamilySamples;
    }
}
