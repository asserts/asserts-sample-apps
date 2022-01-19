/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableList;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Getter
public class ScenarioSimulator extends Collector implements InitializingBean {
    private final CollectorRegistry collectorRegistry;
    private final List<FunctionScenarios> scenarios = new ArrayList<>();

    public ScenarioSimulator(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
        Tenant tenant = Tenant.builder()
                .name("demo")
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

        SQSQueue userQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("UserAnalytics")
                .build();

        SQSQueue paymentQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("PaymentAnalytics")
                .build();

        SQSQueue channelQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("ChannelAnalytics")
                .build();

        SQSQueue ordersQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("Orders")
                .build();

        SQSQueue shipmentQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("Shipments")
                .build();

        SQSQueue returnsQueue = SQSQueue.builder()
                .tenant(tenant)
                .region(region)
                .name("Returns")
                .build();

        Function lakeStaging = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("DataLakeStaging")
                .inputQueue(lakeInput)
                .build();

        Function userAnalyticsService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("UserAnalyticsService")
                .inputQueue(userQueue)
                .outputQueues(ImmutableList.of(lakeInput))
                .build();

        Function paymentsAnalyticsService = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("PaymentAnalyticsService")
                .inputQueue(paymentQueue)
                .outputQueues(ImmutableList.of(lakeInput))
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
                .outputQueues(ImmutableList.of(userQueue, paymentQueue, channelQueue, ordersQueue))
                .build();

        Function orderProcessor = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("OrderProcessor")
                .inputQueue(ordersQueue)
                .outputQueues(ImmutableList.of(shipmentQueue))
                .build();

        Function shipmentProcessor = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("ShipmentProcessor")
                .inputQueue(shipmentQueue)
                .build();

        Function returnProcessor = Function.builder()
                .tenant(tenant)
                .region(region)
                .name("ReturnProcessor")
                .inputQueue(returnsQueue)
                .build();

        scenarios.add(buildCheckoutServiceScenarios(checkoutService));
        scenarios.add(buildChannelAnalyticsScenarios(channelAnalyticsService));
        scenarios.add(buildDataLakeScenarios(lakeStaging));
        scenarios.add(buildPaymentAnalyticsScenarios(paymentsAnalyticsService));
        scenarios.add(buildUserAnalyticsScenarios(userAnalyticsService));
        scenarios.add(buildOrdersService(orderProcessor));
        scenarios.add(buildShipmentService(shipmentProcessor));
        scenarios.add(buildReturnsService(returnProcessor));
    }

    private FunctionScenarios buildDataLakeScenarios(Function lakeStaging) {
        FunctionScenarios lakeStagingScenarios = new FunctionScenarios(lakeStaging);

        NormalState normalState2 = new NormalState(lakeStaging, 600);
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(new Throttle(lakeStaging));
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(new Throttle(lakeStaging));
        lakeStagingScenarios.getSimulators().add(normalState2);
        lakeStagingScenarios.getSimulators().add(normalState2);
        return lakeStagingScenarios;
    }

    private FunctionScenarios buildChannelAnalyticsScenarios(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);

        channelServiceScenarios.getSimulators().add(new NormalState(service, 960));
        channelServiceScenarios.getSimulators().add(new MemorySaturation(service));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildOrdersService(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);

        channelServiceScenarios.getSimulators().add(new NormalState(service, 1000));
        channelServiceScenarios.getSimulators().add(new Latency(service));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildShipmentService(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);

        channelServiceScenarios.getSimulators().add(new NormalState(service, 2000));
        channelServiceScenarios.getSimulators().add(new Latency(service));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildReturnsService(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);
        channelServiceScenarios.getSimulators().add(new NormalState(service, 320));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildPaymentAnalyticsScenarios(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);

        channelServiceScenarios.getSimulators().add(new NormalState(service, 640));
        channelServiceScenarios.getSimulators().add(new MemorySaturation(service));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildUserAnalyticsScenarios(Function service) {
        FunctionScenarios channelServiceScenarios = new FunctionScenarios(service);

        channelServiceScenarios.getSimulators().add(new NormalState(service, 320));
        channelServiceScenarios.getSimulators().add(new MemorySaturation(service));
        return channelServiceScenarios;
    }

    private FunctionScenarios buildCheckoutServiceScenarios(Function service) {
        NormalState normalState = new NormalState(service, 960 * 4);

        FunctionScenarios checkoutServiceScenarios = new FunctionScenarios(service);

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Latency(service));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new MemorySaturation(service));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Throttle(service));

        checkoutServiceScenarios.getSimulators().add(normalState);
        checkoutServiceScenarios.getSimulators().add(new Error(service));
        return checkoutServiceScenarios;
    }

    @Override
    public void afterPropertiesSet() {
        collectorRegistry.register(this);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<>();
        scenarios.forEach(scenario -> samples.addAll(scenario.getMetrics()));
        return samples;
    }

}
