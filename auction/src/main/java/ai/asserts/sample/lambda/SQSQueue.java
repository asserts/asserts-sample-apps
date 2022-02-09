/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Builder
@Getter
public class SQSQueue extends MetricSource {
    private Tenant tenant;
    private Region region;
    private String name;

    List<Collector.MetricFamilySamples> getMetrics(double numMessagesSent) {
        List<Collector.MetricFamilySamples> familySamples = new ArrayList<>();

        SortedMap<String, String> labels = new TreeMap<>(region.labels());
        labels.putAll(tenant.labels());
        labels.put("topic", name);
        labels.put("namespace", "AWS/SQS");
        labels.put("cw_namespace", "AWS/SQS");
        familySamples.add(buildFamily(labels, "aws_sqs_number_of_messages_sent_sum", numMessagesSent));


        Map<String, String> resourceTypeLabels = new TreeMap<>(region.labels());
        resourceTypeLabels.putAll(tenant.labels());
        resourceTypeLabels.put("aws_resource_type", "AWS::SQS::Queue");
        resourceTypeLabels.put("job", name);

        familySamples.add(buildFamily(resourceTypeLabels, "aws_resource", 1.0D));

        return familySamples;
    }
}
