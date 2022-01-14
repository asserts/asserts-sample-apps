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

@Getter
public class FunctionScenarios {
    private Function function;
    private List<BaseSimulator> simulators = new ArrayList<>();
    private int currentSimulator = 0;

    public FunctionScenarios(Function function) {
        this.function = function;
    }

    public List<Collector.MetricFamilySamples> getMetrics() {
        if (simulators.size() > 0) {
            BaseSimulator metricBase = simulators.get(currentSimulator % simulators.size());
            if (metricBase.isDone()) {
                currentSimulator++;
                metricBase = simulators.get(currentSimulator % simulators.size());
            }
            return metricBase.emitMetrics();
        } else {
            return Collections.emptyList();
        }
    }
}
