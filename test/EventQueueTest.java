package core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the EventQueue class.
 * Verifies correct ordering, peeking, and clearing of events.
 */
public class EventQueueTest {

    @Test
    void testEventOrderingByTime() {
        EventQueue queue = new EventQueue();

        // Add events in random order
        queue.addEvent(new Event(5.0, 1, EventType.OFF));
        queue.addEvent(new Event(2.0, 1, EventType.ON));
        queue.addEvent(new Event(7.5, 1, EventType.ON));

        // Verify ordering by simulation time
        Event e1 = queue.nextEvent();
        Event e2 = queue.nextEvent();
        Event e3 = queue.nextEvent();

        assertEquals(2.0, e1.getTime(), 1e-9, "First event should be time 2.0");
        assertEquals(5.0, e2.getTime(), 1e-9, "Second event should be time 5.0");
        assertEquals(7.5, e3.getTime(), 1e-9, "Third event should be time 7.5");
        assertTrue(queue.isEmpty(), "Queue should be empty after polling all events");
    }

    @Test
    void testPeekNextEventDoesNotRemoveIt() {
        EventQueue queue = new EventQueue();
        queue.addEvent(new Event(3.0, 2, EventType.ON));

        Event peeked = queue.peekNextEvent();
        assertNotNull(peeked);
        assertEquals(3.0, peeked.getTime(), 1e-9);
        assertFalse(queue.isEmpty(), "Queue should not be empty after peeking");
    }

    @Test
    void testClearRemovesAllEvents() {
        EventQueue queue = new EventQueue();
        queue.addEvent(new Event(1.0, 1, EventType.ON));
        queue.addEvent(new Event(4.0, 1, EventType.OFF));

        queue.clear();
        assertTrue(queue.isEmpty(), "Queue should be empty after clear()");
        assertEquals(0, queue.size(), "Queue size should be zero after clear()");
    }
}
