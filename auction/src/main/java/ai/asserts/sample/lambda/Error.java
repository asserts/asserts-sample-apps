/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

@SuppressWarnings("unused")
public class Error extends BaseSimulator {
    private final static double delta = 20.0D / 20;
    private double errorRatio = 0.0D;

    public Error(Function function) {
        super(function, 48);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Start at 0% and go to 30% in 30 minutes, hover for 10 minutes and then ramp down to 0% in 30 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 20) {
            errorRatio += delta;
        } else if (28 <= step && step < 48) {
            errorRatio -= delta;
        }
        errorCount = (int) Math.ceil(invocationCount * 0.01 * errorRatio);
        System.out.printf("Invocations Count = %d, Error Count = %d, Error Ratio = %.3f%n",
                invocationCount,
                errorCount,
                0.01 * errorRatio);
        step++;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
        errorRatio = 0.0D;
    }
}
