package extensions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple single-server queue approximation used to capture downstream latency.
 * <p>
 * The queue assumes deterministic service with rate {@code µ} and keeps track
 * of cumulative waiting/system times as well as arrival/serviced counters so
 * that the simulator can report queueing metrics alongside traffic stats.
 */
public class NetworkQueue {

    private final double serviceRate;
    /** Queue capacity (Integer.MAX_VALUE to emulate no limit). */
    private final int capacity = Integer.MAX_VALUE;
    private final Deque<QueueElement> queue = new ArrayDeque<>();

    private double lastProcessedTime = 0.0;
    private double serverBusyUntil = 0.0;
    private long totalArrived = 0;
    private long totalServed = 0;
    private long totalDropped = 0;
    private double sumWaitingTime = 0.0;
    private double sumSystemTime = 0.0;

    /**
     * Creates a queue with a given service rate.
     *
     * @param serviceRate packets served per second (must be &gt; 0)
     */
    public NetworkQueue(double serviceRate) {
        if (serviceRate <= 0)
            throw new IllegalArgumentException("Service rate must be > 0");
        this.serviceRate = serviceRate;
    }

    /**
     * Processes departures and service progress until the specified time.
     *
     * @param t simulation time horizon to advance to
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
     * Enqueues multiple arrivals at the same time instant.
     *
     * @param t     arrival time
     * @param count number of packets to enqueue
     */
    public void enqueueBulk(double t, int count) {
        if (count <= 0) return;
        totalArrived += count;
        for (int i = 0; i < count; i++) {
            queue.addLast(new QueueElement(t));
        }
    }

    /** Wrapper for single-packet enqueue to match Simulator.java */
    public void enqueue(double t) {
        enqueueBulk(t, 1);
    }

    /** @return average waiting time experienced by served packets */
    public double getAvgWaitingTime() { return (totalServed == 0) ? 0 : sumWaitingTime / totalServed; }

    /** @return average total time spent in the system */
    public double getAvgSystemTime() { return (totalServed == 0) ? 0 : sumSystemTime / totalServed; }

    /** @return instantaneous number of packets waiting */
    public int getQueueLength() { return queue.size(); }

    /** @return total packets completed */
    public long getTotalServed() { return totalServed; }

    /** @return packets dropped due to capacity (always zero currently) */
    public long getTotalDropped() { return totalDropped; }

    /** @return cumulative arrivals injected into the queue */
    public long getTotalArrived() { return totalArrived; }

    @Override
    public String toString() {
        return String.format(
                "Queue[arr=%d, served=%d, dropped=%d, avgWait=%.4f, avgSys=%.4f]",
                totalArrived, totalServed, totalDropped, getAvgWaitingTime(), getAvgSystemTime());
    }
}
