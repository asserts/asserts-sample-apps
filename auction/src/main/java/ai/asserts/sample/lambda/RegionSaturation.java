/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.List;

@Getter
public class RegionSaturation extends BaseSimulator {
    private double regionalConcurrency = 30.0D;
    private static final double delta = 65.0D / 40;

    public RegionSaturation(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 108);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        double random = Math.random();
        // Ramp up
        if (step < 40) {
            regionalConcurrency += delta;
        } else if (68 <= step && step < 108) {
            regionalConcurrency -= delta;
        }
        step++;
        return emitMetrics(defaultMemoryUtilization + 5 * random,
                defaultInvocations + (random > 0.5D ? 2 : -2), 0, 0,
                defaultFnExecutionsAvg + random, defaultLatencyP99Ms + random,
                defaultFnExecutionsAvg + random, regionalConcurrency + random);
    }

    @Override
    public void reset() {
        step = 0;
        regionalConcurrency = 30.0D;
    }
}
