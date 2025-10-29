package extensions;

import java.util.ArrayDeque;
import java.util.Deque;

public class NetworkQueue {

    private final double serviceRate;
    private final int capacity;
    private final Deque<QueueElement> queue = new ArrayDeque<>();

    private double lastProcessedTime = 0.0;
    private double serverBusyUntil = 0.0;
    private long totalArrived = 0;
    private long totalServed = 0;
    private long totalDropped = 0;
    private double sumWaitingTime = 0.0;
    private double sumSystemTime = 0.0;

    public NetworkQueue(double serviceRate, int capacity) {
        if (serviceRate <= 0)
            throw new IllegalArgumentException("Service rate must be > 0");
        this.serviceRate = serviceRate;
        this.capacity = capacity;
    }

    public void processUntil(double t) {
        if (t <= lastProcessedTime) return;

        while (!queue.isEmpty()) {
            QueueElement head = queue.peekFirst();
            if (head.serviceStartTime < 0) {
                head.serviceStartTime = Math.max(lastProcessedTime, serverBusyUntil);
                double serviceTime = 1.0 / serviceRate;
                head.departureTime = head.serviceStartTime + serviceTime;
                serverBusyUntil = head.departureTime;
                sumWaitingTime += (head.serviceStartTime - head.arrivalTime);
            }
            if (head.departureTime <= t) {
                sumSystemTime += head.totalTimeInSystem();
                totalServed++;
                queue.removeFirst();
            } else break;
        }
        lastProcessedTime = t;
    }

    public void enqueueBulk(double t, int count) {
        if (count <= 0) return;
        totalArrived += count;
        int space = (capacity <= 0) ? Integer.MAX_VALUE : capacity - queue.size();
        int accepted = Math.min(space, count);
        int dropped = count - accepted;
        totalDropped += Math.max(0, dropped);
        for (int i = 0; i < accepted; i++) queue.addLast(new QueueElement(t));
    }

    public double getAvgWaitingTime() { return (totalServed == 0) ? 0 : sumWaitingTime / totalServed; }
    public double getAvgSystemTime() { return (totalServed == 0) ? 0 : sumSystemTime / totalServed; }

    @Override
    public String toString() {
        return String.format(
                "Queue[arr=%d, served=%d, dropped=%d, avgWait=%.4f, avgSys=%.4f]",
                totalArrived, totalServed, totalDropped, getAvgWaitingTime(), getAvgSystemTime());
    }
}
