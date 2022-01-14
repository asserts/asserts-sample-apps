/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableSortedMap;
import io.prometheus.client.Collector;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Builder
@Getter
public class Region extends MetricSource {
    private Tenant tenant;
    private String name;
    private int concurrencyLimit;

    public SortedMap<String, String> labels() {
        return ImmutableSortedMap.of(
                "cw_namespace", "AWS/Lambda",
                "namespace", "AWS/Lambda",
                "region", name,
                "asserts_site", name,
                "asserts_env", "lambda-demo"
        );
    }

    List<Collector.MetricFamilySamples> getMetrics(double regionExecutionsAvg) {
        List<Collector.MetricFamilySamples> familySamples = new ArrayList<>();

        SortedMap<String, String> labels = new TreeMap<>(labels());
        labels.putAll(tenant.labels());
        familySamples.add(buildFamily(labels, "aws_lambda_concurrent_executions_avg", regionExecutionsAvg));
        familySamples.add(buildFamily(labels, "aws_lambda_account_limit", concurrencyLimit));

        return familySamples;
    }
}
