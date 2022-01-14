/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FunctionTest {
    @Test
    public void getMetrics() {
        Tenant chief = Tenant.builder()
                .name("chief")
                .build();
        Region region = Region.builder()
                .name("us-west-2")
                .tenant(chief)
                .build();

        SQSQueue inputQueue = SQSQueue.builder()
                .name("input")
                .tenant(chief)
                .region(region)
                .build();

        SQSQueue outputQueue = SQSQueue.builder()
                .name("output")
                .tenant(chief)
                .region(region)
                .build();

        Function function = Function.builder()
                .tenant(chief)
                .region(region)
                .name("Test")
                .inputQueue(inputQueue)
                .outputQueues(ImmutableList.of(outputQueue))
                .build();


        assertTrue(function.getMetrics(60, 0, 0, 0,
                0, 0, 0, 0).size() > 0);
    }
}
