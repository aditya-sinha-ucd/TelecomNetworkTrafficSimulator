/**
 * @file src/extensions/NetworkQueue.java
 * @brief Lightweight single-server queue approximation for downstream effects.
 * @details Used by {@link core.Simulator} to translate aggregate ON rates into
 *          latency/throughput metrics. The queue models deterministic service
 *          with configurable rate μ, tracks arrivals, departures, waiting time,
 *          and drop statistics, and exposes averages for reporting. Collaborates
 *          closely with {@link extensions.QueueElement} objects which capture
 *          per-packet timing.
 * @date 2024-05-30
 */
package extensions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @class NetworkQueue
 * @brief Deterministic single-server queue used for coarse congestion modeling.
 * @details Accepts bulk arrivals from {@link core.Simulator}, advances service
 *          progress over time, and aggregates waiting/system time metrics for
 *          diagnostic reporting.
 */
public class NetworkQueue {

    /** Service rate μ expressed in packets per second. */
    private final double serviceRate;
    /** Queue capacity (Integer.MAX_VALUE to emulate no limit). */
    private final int capacity = Integer.MAX_VALUE;
    /** FIFO structure holding pending packets. */
    private final Deque<QueueElement> queue = new ArrayDeque<>();

    private double lastProcessedTime = 0.0;
    private double serverBusyUntil = 0.0;
    private long totalArrived = 0;
    private long totalServed = 0;
    private long totalDropped = 0;
    private double sumWaitingTime = 0.0;
    private double sumSystemTime = 0.0;

    /**
     * @brief Creates a queue with the provided service rate.
     * @param serviceRate Packets served per second (must be &gt; 0).
     * @throws IllegalArgumentException if the service rate is not positive.
     */
    public NetworkQueue(double serviceRate) {
        if (serviceRate <= 0)
            throw new IllegalArgumentException("Service rate must be > 0");
        this.serviceRate = serviceRate;
    }

    /**
     * @brief Processes departures and service progress until the specified time.
     * @param t Simulation time horizon to advance to.
     */
    public void processUntil(double t) {
        if (t <= lastProcessedTime) return;

        while (!queue.isEmpty()) {
            QueueElement head = queue.peekFirst();
            if (head.getServiceStartTime() < 0) {
                head.setServiceStartTime(Math.max(lastProcessedTime, serverBusyUntil));
                double serviceTime = 1.0 / serviceRate;
                head.setDepartureTime(head.getServiceStartTime() + serviceTime);
                serverBusyUntil = head.getDepartureTime();
                sumWaitingTime += (head.getServiceStartTime() - head.getArrivalTime());
            }
            if (head.getDepartureTime() <= t) {
                sumSystemTime += head.getTotalDelay();
                totalServed++;
                queue.removeFirst();
            } else break;
        }
        lastProcessedTime = t;
    }

    /**
     * @brief Enqueues multiple arrivals at the same time instant.
     * @param t Arrival time shared by the bulk arrivals.
     * @param count Number of packets to enqueue.
     */
    public void enqueueBulk(double t, int count) {
        if (count <= 0) return;
        totalArrived += count;
        for (int i = 0; i < count; i++) {
            queue.addLast(new QueueElement(t));
        }
    }

    /**
     * @brief Wrapper for single-packet enqueues to match {@link core.Simulator} usage.
     * @param t Arrival timestamp of the packet.
     */
    public void enqueue(double t) {
        enqueueBulk(t, 1);
    }

    /**
     * @brief Average waiting time experienced by served packets.
     * @return Mean queue-only delay (seconds).
     */
    public double getAvgWaitingTime() {
        return (totalServed == 0) ? 0 : sumWaitingTime / totalServed;
    }

    /**
     * @brief Average total time spent in the system (waiting + service).
     * @return Mean sojourn time for served packets.
     */
    public double getAvgSystemTime() {
        return (totalServed == 0) ? 0 : sumSystemTime / totalServed;
    }

    /**
     * @brief Instantaneous number of packets waiting.
     * @return Queue length including the job in service if any.
     */
    public int getQueueLength() {
        return queue.size();
    }

    /**
     * @brief Total packets completed by the server.
     * @return Cumulative departures count.
     */
    public long getTotalServed() {
        return totalServed;
    }

    /**
     * @brief Packets dropped due to capacity (always zero currently).
     * @return Number of arrivals rejected.
     */
    public long getTotalDropped() {
        return totalDropped;
    }

    /**
     * @brief Cumulative arrivals injected into the queue.
     * @return Total enqueued packet count.
     */
    public long getTotalArrived() {
        return totalArrived;
    }

    /**
     * @brief Provides a human-readable snapshot of queue metrics.
     * @return String summarizing arrivals, departures, drops, and averages.
     */
    @Override
    public String toString() {
        return String.format(
                "Queue[arr=%d, served=%d, dropped=%d, avgWait=%.4f, avgSys=%.4f]",
                totalArrived, totalServed, totalDropped, getAvgWaitingTime(), getAvgSystemTime());
    }
}
