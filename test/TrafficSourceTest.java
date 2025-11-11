package model;

import core.Event;
import core.EventType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TrafficSource class.
 * Verifies ON/OFF transitions and event generation behavior.
 */
public class TrafficSourceTest {

    @Test
    void testInitialStateIsOff() {
        TrafficSource src = new TrafficSource(0, 1.5, 1.0, 1.2, 2.0);
        assertFalse(src.isOn(), "Newly created source should start OFF");
    }

    @Test
    void testProcessEventChangesState() {
        TrafficSource src = new TrafficSource(1, 1.5, 1.0, 1.2, 2.0);
        Event eOn = new Event(0.0, 1, EventType.ON);
        src.processEvent(eOn);
        assertTrue(src.isOn(), "Source should be ON after processing ON event");

        Event eOff = new Event(5.0, 1, EventType.OFF);
        src.processEvent(eOff);
        assertFalse(src.isOn(), "Source should be OFF after processing OFF event");
    }

    @Test
    void testGeneratesNextEvent() {
        TrafficSource src = new TrafficSource(2, 1.5, 1.0, 1.2, 2.0);
        Event next = src.generateNextEvent(0.0);
        assertNotNull(next, "Generated event should not be null");
        assertTrue(next.getTime() > 0, "Generated event should be in the future");
    }
}
