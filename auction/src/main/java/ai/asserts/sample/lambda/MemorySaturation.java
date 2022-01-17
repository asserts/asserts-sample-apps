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
    public static double latencyAvg = 500.0D;
    private double memoryUtilization = 50.0D;

    public MemorySaturation(Function function) {
        super(function, 120);
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

        // Memory saturation causes latency at some point
        latencyAvg = 500.0D + memoryUtilization > 70.0D ? 500.0D * (memoryUtilization - 70.0D) / 50.0D : 0;
        latencyAvgMs = latencyAvg;
        latencyP99Ms = latencyAvgMs + 250.0D;
        memoryUtilizationPct = memoryUtilization;
        step++;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
        memoryUtilization = 50.0D;
    }
}
