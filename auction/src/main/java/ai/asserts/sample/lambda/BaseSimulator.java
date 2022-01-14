/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import io.prometheus.client.Collector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class BaseSimulator {
    protected Function function;
    protected int step = 0;
    protected int maxStep;
    protected int defaultInvocations = 20;
    protected int defaultErrors = 0;
    protected int defaultThrottles = 0;
    protected double defaultMemoryUtilization = 50.0D;
    protected double defaultFnExecutionsAvg = 0.5D;
    protected double defaultRegionalExecutionsAvg = 30;
    protected double defaultLatencyAvgMs = 500;
    protected double defaultLatencyP99Ms = 750;


    public BaseSimulator(Function function,
                         int maxStep) {
        this.function = function;
        this.maxStep = maxStep;
    }

    public boolean isDone() {
        if (step == maxStep) {
            reset();
            return true;
        }
        return false;
    }

    public abstract void reset();

    public List<Collector.MetricFamilySamples> emitMetrics() {
        int scrape_interval = 60;
        List<Collector.MetricFamilySamples> metrics = new ArrayList<>();
        metrics.addAll(function.getMetrics(scrape_interval, defaultMemoryUtilization, defaultInvocations, defaultErrors,
                defaultThrottles, defaultFnExecutionsAvg, defaultLatencyP99Ms, defaultFnExecutionsAvg));
        metrics.addAll(function.getRegion().getMetrics(defaultRegionalExecutionsAvg));
        return metrics;
    }
}
