/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableSortedMap;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.SortedMap;

@Builder
@Getter
public class Tenant {
    private String name;

    public SortedMap<String, String> labels() {
        return new ImmutableSortedMap.Builder<String, String>(Comparator.naturalOrder())
                .put("tenant", name)
                .put("asserts_tenant", name)
                .put("container", "aws-exporter")
                .put("service", "aws-exporter")
                .put("endpoint", "aws")
                .put("instance", "0.8.92.5:8010")
                .put("pod", "aws-exporter-7cbf9fbbd6-b2v6x")
                .build();
    }
}
