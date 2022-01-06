/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.lambda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionSaturationTest {
    @Test
    public void emitMetrics() {
        Service service = new Service("service", "namespace");
        RegionSaturation fixture = new RegionSaturation("Test", 5, 128, service);

        assertEquals(30.0D, fixture.getRegionalConcurrency());
        for (int i = 0; i < 40; i++) {
            assertEquals(i, fixture.getStep());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        assertEquals(95.0D, fixture.getRegionalConcurrency());
        for (int i = 0; i < 28; i++) {
            assertEquals(40 + i, fixture.getStep());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        assertEquals(95.0D, fixture.getRegionalConcurrency());
        for (int i = 0; i < 40; i++) {
            assertEquals(68 + i, fixture.getStep());
            assertFalse(fixture.isDone());
            fixture.emitMetrics();
        }
        assertEquals(30.0D, fixture.getRegionalConcurrency());
        assertEquals(108, fixture.getStep());
        assertTrue(fixture.isDone());
        assertEquals(0, fixture.getStep());
    }
}
