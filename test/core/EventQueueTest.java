/**
 * @file test/core/EventQueueTest.java
 * @brief Verifies chronological ordering and invariants of {@link core.EventQueue}.
 */
package core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class EventQueueTest
 * @brief Unit tests that ensure {@link EventQueue} maintains ordering semantics.
 */
public class EventQueueTest {

    private EventQueue queue;
    private static final double EPSILON = 1e-9;

    /**
     * @brief Creates a new, empty queue before each test to ensure independence.
     */
    @BeforeEach
    void setUp() {
        queue = new EventQueue();
    }

    /**
     * @brief Ensures events added in random order are retrieved chronologically.
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
     * @brief Confirms that peeking at the next event does not remove it.
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
     * @brief Verifies that {@link EventQueue#clear()} removes all events.
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
     * @brief Ensures repeated calls to {@link EventQueue#clear()} are idempotent.
     */
    @Test
    void testClearIsIdempotent() {
        queue.addEvent(new Event(1.0, 1, EventType.ON));
        queue.clear();
        queue.clear(); // Should not throw or alter behavior
        assertTrue(queue.isEmpty(), "Queue should remain empty after multiple clears");
    }

    /**
     * @brief Tests behavior when multiple events share identical timestamps.
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
     * @brief Ensures requesting nextEvent from an empty queue returns {@code null}.
     */
    @Test
    void testNextEventOnEmptyQueueReturnsNull() {
        assertNull(queue.nextEvent(), "nextEvent() should return null when queue is empty");
    }

    /**
     * @brief Ensures peeking into an empty queue returns {@code null}.
     */
    @Test
    void testPeekNextEventOnEmptyQueueReturnsNull() {
        assertNull(queue.peekNextEvent(), "peekNextEvent() should return null when queue is empty");
    }

    /**
     * @brief Verifies that attempting to add a null event throws an exception.
     */
    @Test
    void testAddNullEventThrowsException() {
        assertThrows(NullPointerException.class, () -> queue.addEvent(null),
                "Adding a null event should throw NullPointerException");
    }
}
