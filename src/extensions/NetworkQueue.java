package extensions;

import java.util.ArrayDeque;
import java.util.Deque;

public class NetworkQueue {

    private final double serviceRate;
    private final int capacity = Integer.MAX_VALUE; // default no limit
    private final Deque<QueueElement> queue = new ArrayDeque<>();

    private double lastProcessedTime = 0.0;
    private double serverBusyUntil = 0.0;
    private long totalArrived = 0;
    private long totalServed = 0;
    private long totalDropped = 0;
    private double sumWaitingTime = 0.0;
    private double sumSystemTime = 0.0;

    public NetworkQueue(double serviceRate) {
        if (serviceRate <= 0)
            throw new IllegalArgumentException("Service rate must be > 0");
        this.serviceRate = serviceRate;
    }

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

    public double getAvgWaitingTime() { return (totalServed == 0) ? 0 : sumWaitingTime / totalServed; }
    public double getAvgSystemTime() { return (totalServed == 0) ? 0 : sumSystemTime / totalServed; }

    public int getQueueLength() { return queue.size(); }
    public long getTotalServed() { return totalServed; }
    public long getTotalDropped() { return totalDropped; }

    @Override
    public String toString() {
        return String.format(
                "Queue[arr=%d, served=%d, dropped=%d, avgWait=%.4f, avgSys=%.4f]",
                totalArrived, totalServed, totalDropped, getAvgWaitingTime(), getAvgSystemTime());
    }
}
