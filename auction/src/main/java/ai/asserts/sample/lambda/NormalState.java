/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;

import java.util.List;

public class NormalState extends BaseSimulator {

    public NormalState(Function function) {
        super(function, 40);
    }

    public List<Collector.MetricFamilySamples> emitMetrics() {
        step++;
        return super.emitMetrics();
    }

    @Override
    public void reset() {
        step = 0;
    }
}
