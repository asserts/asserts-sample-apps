/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

@SuppressWarnings("unused")
public class Latency extends BaseSimulator {
    private final static double delta = 4000.0 / 20;
    private double latencyAvg = 500.0D;

    public Latency(Function function) {
        super(function, 48);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Start at 50% and go to 97% in 10 minutes, hover for 10 minutes and then ramp down to 50% in 10 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 20) {
            latencyAvg += delta;
        } else if (28 <= step && step < 48) {
            latencyAvg -= delta;
        }
        latencyAvgMs = latencyAvg;
        latencyP99Ms = latencyAvg + 250.0D;
        step++;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
        latencyAvg = 500.0D;
    }
}
