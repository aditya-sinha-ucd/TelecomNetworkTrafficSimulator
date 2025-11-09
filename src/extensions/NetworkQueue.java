package extensions;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Models a simple FIFO network queue.
 * <p>
 * Can be used to study how bursty traffic affects
 * average delay, waiting time, and queue length.
 */
public class NetworkQueue {

    private final Queue<QueueElement> queue;
    private final double serviceRate;  // packets per second
    private double currentTime;
    private double busyUntil;
    private double totalDelay;
    private int processedPackets;

    /**
     * Constructs a new network queue.
     *
     * @param serviceRate rate of service (packets per second)
     */
    public NetworkQueue(double serviceRate) {
        this.queue = new LinkedList<>();
        this.serviceRate = serviceRate;
        this.currentTime = 0.0;
        this.busyUntil = 0.0;
        this.totalDelay = 0.0;
        this.processedPackets = 0;
    }

    /**
     * Simulates arrival of a packet to the queue.
     *
     * @param arrivalTime time at which packet arrives
     */
    public void enqueue(double arrivalTime) {
        currentTime = arrivalTime;
        QueueElement element = new QueueElement(arrivalTime);

        if (arrivalTime >= busyUntil) {
            // Server is idle, start service immediately
            element.setServiceStartTime(arrivalTime);
        } else {
            // Server busy, wait until it’s free
            element.setServiceStartTime(busyUntil);
        }

        double serviceDuration = 1.0 / serviceRate;
        element.setDepartureTime(element.getServiceStartTime() + serviceDuration);
        busyUntil = element.getDepartureTime();

        queue.add(element);
        totalDelay += element.getTotalDelay();
        processedPackets++;
    }

    /**
     * @return average delay (waiting + service)
     */
    public double getAverageDelay() {
        return processedPackets == 0 ? 0.0 : totalDelay / processedPackets;
    }

    /**
     * @return number of packets processed so far
     */
    public int getProcessedPackets() {
        return processedPackets;
    }

    /**
     * @return the number of packets currently in queue
     */
    public int getQueueLength() {
        return queue.size();
    }

    /** Resets the queue metrics. */
    public void reset() {
        queue.clear();
        currentTime = 0.0;
        busyUntil = 0.0;
        totalDelay = 0.0;
        processedPackets = 0;
    }
}
