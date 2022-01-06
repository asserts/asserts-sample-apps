/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

@SuppressWarnings("unused")
public class Latency extends BaseSimulator {
    private final static double delta = 4000.0 / 40;
    private double latencyAvg = 500.0D;

    public Latency(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 100);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Start at 50% and go to 97% in 10 minutes, hover for 10 minutes and then ramp down to 50% in 10 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 40) {
            latencyAvg += delta;
        } else if (60 <= step && step < 100) {
            latencyAvg -= delta;
        }
        step++;
        return emitMetrics(defaultMemoryUtilization + 5 * random,
                defaultInvocations + (random > 0.5D ? 2 : -2), 0, 0,
                latencyAvg + random,
                latencyAvg + 250.0D + random,
                defaultFnExecutionsAvg + random,
                defaultRegionalExecutionsAvg + random);
    }

    @Override
    public void reset() {
        step = 0;
        latencyAvg = 500.0D;
    }
}
