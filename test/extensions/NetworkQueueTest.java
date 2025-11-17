/**
 * @file test/extensions/NetworkQueueTest.java
 * @brief Verifies the behavior of the {@link extensions.NetworkQueue} helper.
 * @details Covers queue length, service progression, drop counts, and metric
 *          consistency for the unlimited-capacity queue used within the
 *          simulator.
 */
package extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class NetworkQueueTest
 * @brief Unit tests targeting {@link extensions.NetworkQueue} invariants.
 */
public class NetworkQueueTest {

    /**
     * @brief Ensures packets added to the queue increase its length.
     */
    @Test
    void testEnqueueIncreasesQueueLength() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 3);

        assertEquals(3, queue.getQueueLength(),
                "Queue length should reflect the number of packets enqueued");
    }

    /**
     * @brief Verifies that processing time causes packets to be served.
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
     * @brief Confirms that totalDropped remains zero for unlimited capacity.
     */
    @Test
    void testNoPacketsDroppedInUnlimitedQueue() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 1000);

        assertEquals(0, queue.getTotalDropped(),
                "Unlimited-capacity queue should never drop packets");
    }

    /**
     * @brief Ensures queue statistics remain consistent after multiple updates.
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
