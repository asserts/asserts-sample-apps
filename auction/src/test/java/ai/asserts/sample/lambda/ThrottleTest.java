/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThrottleTest {
    @Test
    public void emitMetrics() {
        Service service = new Service("service", "namespace");
        Throttle fixture = new Throttle("Test", 5, 128, service);

        for (int i = 0; i < 60; i++) {
            assertEquals(i, fixture.getStep());
            assertEquals(20 + i * 4, fixture.getInvocations());
            assertEquals(Math.max(fixture.getInvocations() - Throttle.throttleThreshold, 0), fixture.getThrottles());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        for (int i = 0; i < 60; i++) {
            assertEquals(i + 60, fixture.getStep());
            assertEquals(260 - i * 4, fixture.getInvocations());
            assertEquals(Math.max(fixture.getInvocations() - Throttle.throttleThreshold, 0), fixture.getThrottles());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        assertEquals(120, fixture.getStep());
        assertTrue(fixture.isDone());
        assertEquals(0, fixture.getStep());
    }
}
