package extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NetworkQueue class (unlimited capacity version).
 *
 * This version matches the provided NetworkQueue implementation, which:
 *  - only has a single constructor (serviceRate)
 *  - does not drop packets (unlimited capacity)
 *  - tracks served packets and queue length
 */
public class NetworkQueueTest {

    /**
     * Test that packets added to the queue increase its length.
     */
    @Test
    void testEnqueueIncreasesQueueLength() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 3);

        assertEquals(3, queue.getQueueLength(),
                "Queue length should reflect the number of packets enqueued");
    }

    /**
     * Test that processing time causes packets to be served.
     */
    @Test
    void testPacketsAreServedOverTime() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 5);

        // Process 1 second of simulated time
        queue.processUntil(1.0);

        assertTrue(queue.getTotalServed() > 0,
                "Some packets should have been served after processing for 1s");
    }

    /**
     * Test that totalDropped remains zero (unlimited capacity, no drops).
     */
    @Test
    void testNoPacketsDroppedInUnlimitedQueue() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 1000);

        assertEquals(0, queue.getTotalDropped(),
                "Unlimited-capacity queue should never drop packets");
    }

    /**
     * Test that queue statistics remain consistent after several updates.
     */
    @Test
    void testQueueStatsConsistency() {
        NetworkQueue queue = new NetworkQueue(10.0);

        queue.enqueueBulk(0.0, 5);
        queue.processUntil(2.0);
        queue.enqueueBulk(2.0, 5);
        queue.processUntil(4.0);

        assertTrue(queue.getTotalServed() >= 0, "Served packets count should never be negative");
        assertTrue(queue.getTotalDropped() >= 0, "Dropped packets count should never be negative");
        assertTrue(queue.getQueueLength() >= 0, "Queue length should never be negative");
    }
}
