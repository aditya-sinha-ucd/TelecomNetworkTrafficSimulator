package extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NetworkQueue class.
 *
 * This version assumes the class includes these simple getter methods:
 *   public int getQueueLength() { return queue.size(); }
 *   public long getTotalServed() { return totalServed; }
 *   public long getTotalDropped() { return totalDropped; }
 *
 * These getters are purely read-only (safe to add) and make the queue
 * easier to inspect in tests and other simulations.
 */
public class NetworkQueueTest {

    /**
     * Test that packets added to the queue increase its length.
     */
    @Test
    void testEnqueueIncreasesQueueLength() {
        NetworkQueue queue = new NetworkQueue(10.0, 5);
        queue.enqueueBulk(0.0, 3);

        assertEquals(3, queue.getQueueLength(),
                "Queue length should reflect the number of packets enqueued");
    }

    /**
     * Test that processing time causes packets to be served.
     */
    @Test
    void testPacketsAreServedOverTime() {
        NetworkQueue queue = new NetworkQueue(10.0, 5);
        queue.enqueueBulk(0.0, 5);

        // Process 1 second of simulated time
        queue.processUntil(1.0);

        assertTrue(queue.getTotalServed() > 0,
                "Some packets should have been served after processing for 1s");
    }

    /**
     * Test that packets exceeding capacity are dropped.
     */
    @Test
    void testPacketsDroppedWhenFull() {
        NetworkQueue queue = new NetworkQueue(10.0, 5);
        queue.enqueueBulk(0.0, 10);  // capacity = 5 → should drop some

        assertTrue(queue.getTotalDropped() > 0,
                "Packets should be dropped when the queue is full");
    }

    /**
     * Test that queue statistics remain consistent after several updates.
     */
    @Test
    void testQueueStatsConsistency() {
        NetworkQueue queue = new NetworkQueue(10.0, 5);

        // Add and process some packets
        queue.enqueueBulk(0.0, 5);
        queue.processUntil(2.0);
        queue.enqueueBulk(2.0, 5);
        queue.processUntil(4.0);

        // Basic consistency checks
        assertTrue(queue.getTotalServed() >= 0, "Served packets count should never be negative");
        assertTrue(queue.getTotalDropped() >= 0, "Dropped packets count should never be negative");
        assertTrue(queue.getQueueLength() >= 0, "Queue length should never be negative");
    }
}
