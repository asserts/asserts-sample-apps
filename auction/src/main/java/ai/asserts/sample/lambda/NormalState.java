/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

public class NormalState extends BaseSimulator {

    public NormalState(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 40);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        double random = Math.random();
        step++;
        return emitMetrics(defaultMemoryUtilization + 5 * random,
                defaultInvocations + (random > 0.5D ? 2 : -2), 0, 0,
                defaultLatencyAvgMs + random, defaultLatencyP99Ms + random,
                defaultFnExecutionsAvg + random, defaultRegionalExecutionsAvg + random);
    }

    @Override
    public void reset() {
        step = 0;
    }
}
