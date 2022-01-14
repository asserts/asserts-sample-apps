/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.List;

@Getter
public class RegionSaturation extends NormalState {
    private double regionalConcurrency = 30.0D;
    private static final double delta = 65.0D / 40;

    public RegionSaturation(Function function) {
        super(function);
        this.maxStep = 108;
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        // Ramp up
        if (step < 40) {
            regionalConcurrency += delta;
        } else if (68 <= step && step < 108) {
            regionalConcurrency -= delta;
        }
        defaultRegionalExecutionsAvg = regionalConcurrency;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
        regionalConcurrency = 30.0D;
    }
}
