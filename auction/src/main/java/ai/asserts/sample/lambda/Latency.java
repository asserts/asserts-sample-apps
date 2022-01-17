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

    public Latency(Function function) {
        super(function, 100);
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
