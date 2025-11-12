package core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EventQueue class.
 * These tests verify the correct behavior of the event queue used for
 * time-ordered simulation scheduling. The queue should:
 * - Maintain strict chronological ordering of events.
 * - Support safe peeking without removing elements.
 * - Handle empty and duplicate-time cases gracefully.
 * - Correctly clear and reset its internal state.
 */
public class EventQueueTest {

    private EventQueue queue;
    private static final double EPSILON = 1e-9;

    /**
     * Creates a new, empty queue before each test to ensure independence.
     */
    @BeforeEach
    void setUp() {
        queue = new EventQueue();
    }

    /**
     * Ensures that events added in random order are retrieved
     * in ascending order of their scheduled time.
     * The test adds three events at times 5.0, 2.0, and 7.5, and
     * verifies that nextEvent() returns them in the correct order.
     * It also checks that the queue becomes empty afterward.
     */
    @Test
    void testEventOrderingByTime() {
        // Add events in non-chronological order
        queue.addEvent(new Event(5.0, 1, EventType.OFF));
        queue.addEvent(new Event(2.0, 1, EventType.ON));
        queue.addEvent(new Event(7.5, 1, EventType.ON));

        // Retrieve events sequentially
        Event e1 = queue.nextEvent();
        Event e2 = queue.nextEvent();
        Event e3 = queue.nextEvent();

        // Verify chronological order and correct clearing of events at the end
        assertEquals(2.0, e1.getTime(), EPSILON, "First event should be at time 2.0");
        assertEquals(5.0, e2.getTime(), EPSILON, "Second event should be at time 5.0");
        assertEquals(7.5, e3.getTime(), EPSILON, "Third event should be at time 7.5");
        assertTrue(queue.isEmpty(), "Queue should be empty after polling all events");
    }

    /**
     * Confirms that peeking at the next event does not remove it
     * from the queue.
     * The test adds a single event, calls peekNextEvent(),
     * and verifies that:
     * - The returned event matches the expected time and type.
     * - The queue still contains the event afterward.
     */
    @Test
    void testPeekNextEventDoesNotRemoveIt() {
        Event e = new Event(3.0, 2, EventType.ON);
        queue.addEvent(e);

        Event peeked = queue.peekNextEvent();

        assertNotNull(peeked, "Peeked event should not be null");
        assertEquals(3.0, peeked.getTime(), EPSILON, "Peeked event should have time 3.0");
        assertEquals(EventType.ON, peeked.getType(), "Peeked event type should match");
        assertFalse(queue.isEmpty(), "Queue should not be empty after peeking");
        assertEquals(1, queue.size(), "Queue size should remain unchanged after peeking");
    }

    /**
     * Verifies that the clear() method completely removes
     * all events from the queue and resets its size to zero.
     */
    @Test
    void testClearRemovesAllEvents() {
        queue.addEvent(new Event(1.0, 1, EventType.ON));
        queue.addEvent(new Event(4.0, 1, EventType.OFF));

        queue.clear();

        assertTrue(queue.isEmpty(), "Queue should be empty after clear()");
        assertEquals(0, queue.size(), "Queue size should be zero after clear()");
    }

    /**
     * Ensures that calling clear() multiple times is safe
     * and does not throw exceptions.
     */
    @Test
    void testClearIsIdempotent() {
        queue.addEvent(new Event(1.0, 1, EventType.ON));
        queue.clear();
        queue.clear(); // Should not throw or alter behavior
        assertTrue(queue.isEmpty(), "Queue should remain empty after multiple clears");
    }

    /**
     * Tests behavior when multiple events have identical timestamps.
     * The queue should preserve insertion order (FIFO) among events
     * with the same time value.
     */
    @Test
    void testEventsWithSameTimestampKeepInsertionOrder() {
        Event e1 = new Event(5.0, 1, EventType.ON);
        Event e2 = new Event(5.0, 2, EventType.OFF);

        queue.addEvent(e1);
        queue.addEvent(e2);

        assertSame(e1, queue.nextEvent(), "First inserted event should be returned first");
        assertSame(e2, queue.nextEvent(), "Second inserted event should be returned second");
        assertTrue(queue.isEmpty(), "Queue should be empty after removing both events");
    }

    /**
     * Ensures that requesting the next event from an empty queue
     * returns null instead of throwing an exception.
     */
    @Test
    void testNextEventOnEmptyQueueReturnsNull() {
        assertNull(queue.nextEvent(), "nextEvent() should return null when queue is empty");
    }

    /**
     * Ensures that peeking into an empty queue returns null
     * instead of throwing an exception.
     */
    @Test
    void testPeekNextEventOnEmptyQueueReturnsNull() {
        assertNull(queue.peekNextEvent(), "peekNextEvent() should return null when queue is empty");
    }

    /**
     *
     * Verifies that attempting to add a null event throws an exception,
     * ensuring the queue cannot contain invalid entries.
     */
    @Test
    void testAddNullEventThrowsException() {
        assertThrows(NullPointerException.class, () -> queue.addEvent(null),
                "Adding a null event should throw NullPointerException");
    }
}
