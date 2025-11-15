package model;

import core.Event;
import core.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrafficSource} verifying the ON/OFF state machine
 * and event scheduling logic.
 */
public class TrafficSourceTest {

    private TrafficSource source;
    private static final double EPSILON = 1e-9;

    @BeforeEach
    void setUp() {
        Distribution onDist = new FixedDistribution(2.0, 3.0);
        Distribution offDist = new FixedDistribution(5.0, 4.0);
        source = new TrafficSource(7, onDist, offDist);
    }

    @Test
    void testGenerateNextEventSchedulesOnThenOff() {
        Event first = source.generateNextEvent(10.0);
        assertEquals(EventType.ON, first.getType());
        assertEquals(12.0, first.getTime(), EPSILON);
        assertEquals(12.0, source.getNextEventTime(), EPSILON);

        source.processEvent(first);
        assertTrue(source.isOn());

        Event second = source.generateNextEvent(first.getTime());
        assertEquals(EventType.OFF, second.getType());
        assertEquals(17.0, second.getTime(), EPSILON);
        assertEquals(17.0, source.getNextEventTime(), EPSILON);
    }

    @Test
    void testProcessEventSwitchesStates() {
        Event onEvent = new Event(1.0, source.getId(), EventType.ON);
        source.processEvent(onEvent);
        assertTrue(source.isOn(), "Source should be ON after ON event");

        Event offEvent = new Event(3.0, source.getId(), EventType.OFF);
        source.processEvent(offEvent);
        assertFalse(source.isOn(), "Source should be OFF after OFF event");
    }

    @Test
    void testAlternatingSamplesAdvanceTimeMonotonically() {
        Event on = source.generateNextEvent(0.0);
        source.processEvent(on);

        Event off = source.generateNextEvent(on.getTime());
        source.processEvent(off);

        Event onAgain = source.generateNextEvent(off.getTime());

        assertTrue(onAgain.getTime() > off.getTime(),
                "Next ON event must be scheduled after OFF duration");
        assertEquals(EventType.ON, onAgain.getType());
    }

    /** Deterministic distribution returning pre-defined samples. */
    private static final class FixedDistribution extends Distribution {
        private final double[] values;
        private int index = 0;

        private FixedDistribution(double... values) {
            this.values = values;
        }

        @Override
        public double sample() {
            if (index >= values.length) {
                throw new IllegalStateException("No more samples available");
            }
            return values[index++];
        }
    }
}
