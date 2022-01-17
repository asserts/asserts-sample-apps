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
    protected int invocationCount = 20;
    protected int errorCount = 0;
    protected int throttleCount = 0;
    protected double memoryUtilizationPct = 50.0D;
    protected double fnExecutionCountAvg = 0.5D;
    protected double regionalExecutionCountAvg = 30;
    protected double latencyAvgMs = 500;
    protected double latencyP99Ms = 750;


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
        metrics.addAll(function.getMetrics(scrape_interval, memoryUtilizationPct, invocationCount, errorCount,
                throttleCount, latencyAvgMs, latencyP99Ms, fnExecutionCountAvg));
        metrics.addAll(function.getRegion().getMetrics(regionalExecutionCountAvg));
        return metrics;
    }
}
