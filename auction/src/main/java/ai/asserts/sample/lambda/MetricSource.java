/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableList;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import static io.prometheus.client.Collector.Type.GAUGE;

public class MetricSource {
    public Collector.MetricFamilySamples buildFamily(Map<String, String> functionLabels, String metricName, double metricValue) {
        return new Collector.MetricFamilySamples(metricName, GAUGE, "",
                ImmutableList.of(new Collector.MetricFamilySamples.Sample(metricName,
                        new ArrayList<>(functionLabels.keySet()),
                        new ArrayList<>(functionLabels.values()),
                        metricValue)));
    }

    protected Collector.MetricFamilySamples metricSample(Collector.Type type, SortedMap<String, String> copy,
                                                         String metricName, double value) {
        return new Collector.MetricFamilySamples(metricName, type, "",
                Collections.singletonList(new Collector.MetricFamilySamples.Sample(
                        metricName,
                        new ArrayList<>(copy.keySet()),
                        new ArrayList<>(copy.values()),
                        value)));
    }
}
