/**
 * @file test/extensions/NetworkQueueTest.java
 * @brief Verifies the behavior of the {@link extensions.NetworkQueue} helper.
 * @details Covers queue length, service progression, drop counts, validation checks,
 *          average metrics, idempotence, and output formatting for the unlimited-capacity
 *          queue used within the simulator.
 */
package extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class NetworkQueueTest
 * @brief Unit tests targeting {@link extensions.NetworkQueue} invariants and behaviors.
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

    // =====================================================================
    // Additional coverage tests
    // =====================================================================

    /**
     * @brief Ensures the constructor rejects non-positive service rates.
     */
    @Test
    void testConstructorRejectsInvalidRate() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkQueue(0.0),
                "Zero service rate should be rejected");
        assertThrows(IllegalArgumentException.class, () -> new NetworkQueue(-1.0),
                "Negative service rate should be rejected");
    }

    /**
     * @brief Verifies that the single enqueue wrapper adds one packet correctly.
     */
    @Test
    void testSingleEnqueueAddsPacket() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueue(0.0);

        assertEquals(1, queue.getQueueLength(), "Queue should have one packet");
        assertEquals(1, queue.getTotalArrived(), "Total arrivals should be one");
    }

    /**
     * @brief Ensures invalid enqueue counts (zero or negative) are ignored.
     */
    @Test
    void testInvalidEnqueueCountsIgnored() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 0);
        queue.enqueueBulk(0.0, -5);

        assertEquals(0, queue.getQueueLength(), "Queue should ignore invalid counts");
        assertEquals(0, queue.getTotalArrived(), "No arrivals should be recorded");
    }

    /**
     * @brief Verifies that average waiting and system times are valid after serving packets.
     */
    @Test
    void testAverageTimesAreValid() {
        NetworkQueue queue = new NetworkQueue(5.0);
        queue.enqueueBulk(0.0, 3);
        queue.processUntil(2.0);

        assertTrue(queue.getAvgSystemTime() >= queue.getAvgWaitingTime(),
                "System time should be >= waiting time");
        assertTrue(queue.getAvgSystemTime() >= 0, "System time should be non-negative");
    }

    /**
     * @brief Confirms total arrivals counter matches total enqueued packets.
     */
    @Test
    void testTotalArrivalsCount() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 2);
        queue.enqueueBulk(1.0, 3);

        assertEquals(5, queue.getTotalArrived(), "Total arrivals should equal total enqueued");
    }

    /**
     * @brief Ensures repeated processUntil() calls with same time do not alter the state.
     */
    @Test
    void testProcessUntilSameTimeNoChange() {
        NetworkQueue queue = new NetworkQueue(5.0);
        queue.enqueueBulk(0.0, 4);
        queue.processUntil(1.0);
        long served = queue.getTotalServed();

        queue.processUntil(1.0); // same time
        assertEquals(served, queue.getTotalServed(),
                "Processing at same time should not change served count");
    }

    /**
     * @brief Ensures that the string summary output contains expected metrics.
     */
    @Test
    void testToStringHasExpectedFormat() {
        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 5);
        queue.processUntil(1.0);

        String s = queue.toString();
        assertTrue(s.contains("Queue["), "Output should contain prefix 'Queue['");
        assertTrue(s.contains("arr=") && s.contains("served="),
                "Output should show arrivals and served counts");
    }
}
