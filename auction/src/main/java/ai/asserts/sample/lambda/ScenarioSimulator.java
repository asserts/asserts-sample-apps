/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unused")
@Component
public class ScenarioSimulator extends Collector implements InitializingBean {
    private final CollectorRegistry collectorRegistry;

    public static final Map<String, String> TENANT_ENV_LABELS = new ImmutableMap.Builder<String, String>()
            .put("asserts_env", "dev")
            .put("asserts_site", "dev")
            .put("asserts_tenant", "chief")
            .build();

    @Setter
    private List<BaseSimulator> simulators;
    private int currentSimulator = 0;

    public ScenarioSimulator(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;

        Service service = new Service("DiscountService", "promotion-services");
        NormalState normal = new NormalState("CheckoutService", 5, 128, service);

        simulators = new ArrayList<>();
        simulators.add(normal);
        simulators.add(new Error("CheckoutService", 5, 128, service));
        simulators.add(normal);
        simulators.add(new MemorySaturation("CheckoutService", 5, 128, service));
        simulators.add(normal);
        simulators.add(new Latency("CheckoutService", 5, 128, service));
        simulators.add(normal);
        simulators.add(new Throttle("CheckoutService", 5, 128, service));
        simulators.add(normal);
        simulators.add(new RegionSaturation("CheckoutService", 5, 128, service));
        simulators.add(normal);
    }

    @Override
    public void afterPropertiesSet() {
        collectorRegistry.register(this);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        BaseSimulator metricBase = simulators.get(currentSimulator % simulators.size());
        if (metricBase.isDone()) {
            currentSimulator++;
            metricBase = simulators.get(currentSimulator % simulators.size());
        }
        return metricBase.emitMetrics();
    }

}
