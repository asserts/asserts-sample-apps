/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.List;

@Getter
public class Throttle extends BaseSimulator {
    public final static double concurrencyDelta = 0.1D;
    public final static int invocationsDelta = 4;
    public final static int throttleThreshold = 180;
    private int invocations = 20;
    private double concurrency = 1.0D;
    private int throttles = 0;

    public Throttle(String name, Integer timeoutSeconds, Integer memoryMb, Service callsService) {
        super(name, timeoutSeconds, memoryMb, callsService, 120);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Make invocation rate 5x in 10 minutes
        // Start Throttling at 10 minutes
        // Let throttling continue till 15 minutes
        // Ramp down to normal invocation in 10 minutes
        // Emit metrics every second
        double random = Math.random();
        if (step < 40) {
            invocations += invocationsDelta;
            concurrency += concurrencyDelta;
        } else if (step < 60) {
            invocations += invocationsDelta;
            throttles = Math.max(invocations - throttleThreshold, 0);
        } else if (step < 100) {
            invocations -= invocationsDelta;
            throttles = Math.max(invocations - throttleThreshold, 0);
            concurrency -= concurrencyDelta;
        } else if (step < 120) {
            invocations -= invocationsDelta;
        }
        step++;
        return emitMetrics(defaultMemoryUtilization + 5 * random,
                invocations, 0, throttles,
                defaultLatencyAvgMs + random, defaultLatencyP99Ms + random,
                concurrency, defaultRegionalExecutionsAvg + concurrency);
    }

    @Override
    public void reset() {
        step = 0;
        concurrency = 1.0D;
        invocations = 20;
    }
}
