/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.prometheus.client.Collector.Type.GAUGE;

@AllArgsConstructor
@Getter
public class Service {
    private final String name;
    private final String namespace;

    public List<Collector.MetricFamilySamples> getUpAndLatencyMetric(double latencyValue) {
        List<Collector.MetricFamilySamples> metricFamilySamples = new ArrayList<>();
        Map<String, String> labels = new TreeMap<>();
        labels.put("job", name);
        labels.put("container", name);
        labels.put("namespace", namespace);
        labels.put("asserts_tenant", "chief");
        labels.put("tenant", "chief");
        labels.putAll(ScenarioSimulator.TENANT_ENV_LABELS);

        metricFamilySamples.add(new Collector.MetricFamilySamples("up", GAUGE, "",
                Collections.singletonList(new Collector.MetricFamilySamples.Sample(
                        "up",
                        new ArrayList<>(labels.keySet()),
                        new ArrayList<>(labels.values()),
                        1.0D))));

        labels.put("asserts_entity_type", "Service");
        labels.put("asserts_request_type", "inbound");
        labels.put("asserts_source", "springboot");
        metricFamilySamples.add(new Collector.MetricFamilySamples("asserts:latency:average", GAUGE, "",
                Collections.singletonList(new Collector.MetricFamilySamples.Sample(
                        "asserts:latency:avg",
                        new ArrayList<>(labels.keySet()),
                        new ArrayList<>(labels.values()),
                        latencyValue))));
        return metricFamilySamples;
    }
}
