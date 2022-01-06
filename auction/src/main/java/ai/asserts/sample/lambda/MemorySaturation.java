/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.List;

@SuppressWarnings("unused")
@Getter
public class MemorySaturation extends BaseSimulator {
    public static final double delta = 47.0D / 40;
    public static final double latencyAvg = 500.0D;
    private double memoryUtilization = 50.0D;

    public MemorySaturation(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 120);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Start at 50% and go to 97% in 10 minutes, hover for 10 minutes and then ramp down to 50% in 10 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 40) {
            memoryUtilization += delta;
        } else if (80 <= step && step < 120) {
            memoryUtilization -= delta;
        }
        step++;
        return emitMetrics(memoryUtilization,
                defaultInvocations + random > 0.5D ? 2 : -2, 0, 0,
                latencyAvg + random,
                defaultLatencyP99Ms + random,
                defaultFnExecutionsAvg + random,
                defaultRegionalExecutionsAvg + random);
    }

    @Override
    public void reset() {
        step = 0;
        memoryUtilization = 50.0D;
    }
}
