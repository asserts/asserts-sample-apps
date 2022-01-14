/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemorySaturationTest {
    @Test
    public void emitMetrics() {
        Tenant chief = Tenant.builder()
                .name("chief")
                .build();
        Function function = Function.builder()
                .name("function")
                .region(Region.builder()
                        .name("us-west-2")
                        .tenant(chief)
                        .build())
                .tenant(chief)
                .build();
        MemorySaturation fixture = new MemorySaturation(function);

        double memory = fixture.getMemoryUtilization();
        for (int i = 0; i < 40; i++) {
            assertEquals(i, fixture.getStep());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
            assertEquals(memory + MemorySaturation.delta, fixture.getMemoryUtilization());
            memory = fixture.getMemoryUtilization();
        }
        for (int i = 0; i < 40; i++) {
            assertEquals(i + 40, fixture.getStep());
            assertEquals(memory, fixture.getMemoryUtilization());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        for (int i = 0; i < 40; i++) {
            assertEquals(i + 80, fixture.getStep());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
            assertEquals(memory - MemorySaturation.delta, fixture.getMemoryUtilization());
            memory = fixture.getMemoryUtilization();
        }
        assertEquals(120, fixture.getStep());
        assertTrue(fixture.isDone());
        assertEquals(0, fixture.getStep());
    }
}
