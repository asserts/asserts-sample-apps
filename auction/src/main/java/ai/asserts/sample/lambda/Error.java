/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

@SuppressWarnings("unused")
public class Error extends BaseSimulator {
    private final static double delta = 8.0D / 120;
    private double errorRate = 0.0D;

    public Error(Function function) {
        super(function, 280);
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
        errorCount = (int) Math.ceil(invocationCount * 0.01 * errorRate);
        step++;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
        errorRate = 0.0D;
    }
}
