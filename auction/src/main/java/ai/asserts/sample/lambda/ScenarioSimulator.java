/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableList;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SuppressWarnings("unused")
@Component
public class ScenarioSimulator extends Collector implements InitializingBean {
    private final CollectorRegistry collectorRegistry;
    private List<FunctionScenarios> scenarios = new ArrayList<>();


    public ScenarioSimulator(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;

        Tenant tenant = Tenant.builder()
                .name("chief")
                .build();

        Region region = Region.builder()
                .name("us-west-2")
                .tenant(tenant)
                .build();

        Service discountService = new Service(tenant, "DiscountService", "promotion-services");

        SQSQueue lakeInput = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("DataLakeInput")
                .build();

        Function lakeStaging = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("DataLakeStaging")
                .inputQueue(lakeInput)
                .build();

        SQSQueue userQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("UserAnalytics")
                .build();

        Function userAnalyticsService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("UserAnalyticsService")
                .inputQueue(userQueue)
                .outputQueues(ImmutableList.of(lakeInput))
                .build();

        SQSQueue paymentQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("PaymentAnalytics")
                .build();

        Function paymentsAnalyticsService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("PaymentAnalyticsService")
                .inputQueue(paymentQueue)
                .outputQueues(ImmutableList.of(lakeInput))
                .build();

        SQSQueue channelQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("ChannelAnalytics")
                .build();

        Function channelAnalyticsService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("ChannelAnalyticsService")
                .inputQueue(channelQueue)
                .outputQueues(ImmutableList.of(lakeInput))
                .build();

        Function checkoutService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("CheckoutService")
                .callsServices(ImmutableList.of(discountService))
                .outputQueues(ImmutableList.of(userQueue, paymentQueue, channelQueue))
                .build();

        scenarios.add(buildCheckoutServiceScenarios(checkoutService));
        scenarios.add(buildChannelAnalyticsScenarios(channelAnalyticsService));
        scenarios.add(buildDataLakeScenarios(lakeStaging));
        scenarios.add(buildPaymentAnalyticsScenarios(paymentsAnalyticsService));
        scenarios.add(buildUserAnalyticsScenarios(userAnalyticsService));
    }

    private FunctionScenarios buildDataLakeScenarios(Function lakeStaging) {
        FunctionScenarios lakeStagingScenarios = new FunctionScenarios(lakeStaging);

        NormalState normalState2 = new NormalState(lakeStaging);
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(new Throttle(lakeStaging));
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(new Throttle(lakeStaging));
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(normalState2);
        return lakeStagingScenarios;
    }

    private FunctionScenarios buildChannelAnalyticsScenarios(Function channelAnalyticsService) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(channelAnalyticsService);

        NormalState normalState1 = new NormalState(channelAnalyticsService);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(new MemorySaturation(channelAnalyticsService));
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        return channelServiceScenarios;
    }

    private FunctionScenarios buildPaymentAnalyticsScenarios(Function channelAnalyticsService) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(channelAnalyticsService);

        NormalState normalState1 = new NormalState(channelAnalyticsService);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(new MemorySaturation(channelAnalyticsService));
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        return channelServiceScenarios;
    }

    private FunctionScenarios buildUserAnalyticsScenarios(Function channelAnalyticsService) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(channelAnalyticsService);

        NormalState normalState1 = new NormalState(channelAnalyticsService);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(normalState1);
        channelServiceScenarios.getSimulators().add(new MemorySaturation(channelAnalyticsService));
        channelServiceScenarios.getSimulators().add(normalState1);
        return channelServiceScenarios;
    }

    private FunctionScenarios buildCheckoutServiceScenarios(Function checkoutService) {
        NormalState normalState = new NormalState(checkoutService);

        FunctionScenarios checkoutServiceScenarios = new FunctionScenarios(checkoutService);

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Error(checkoutService));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Latency(checkoutService));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new MemorySaturation(checkoutService));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Throttle(checkoutService));
        return checkoutServiceScenarios;
    }

    @Override
    public void afterPropertiesSet() {
        collectorRegistry.register(this);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<>();
        scenarios.forEach(scenario -> {
            samples.addAll(scenario.getMetrics());
        });
        return samples;
    }

}
