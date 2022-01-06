/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

@SuppressWarnings("unused")
public class Error extends BaseSimulator {
    private final static double delta = 30.0D / 120;
    private double errorRate = 0.0D;

    public Error(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 280);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Start at 0% and go to 30% in 30 minutes, hover for 10 minutes and then ramp down to 0% in 30 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 120) {
            errorRate += delta;
        } else if (160 <= step && step < 280) {
            errorRate -= delta;
        }
        step++;
        return emitMetrics(defaultMemoryUtilization + 5 * random,
                defaultInvocations + (random > 0.5D ? 2 : -2),
                (int) Math.ceil(defaultInvocations * errorRate * 0.01),
                0,
                defaultLatencyAvgMs + random,
                defaultLatencyP99Ms + random,
                defaultFnExecutionsAvg + random,
                defaultRegionalExecutionsAvg + random);
    }

    @Override
    public void reset() {
        step = 0;
        errorRate = 0.0D;
    }
}
