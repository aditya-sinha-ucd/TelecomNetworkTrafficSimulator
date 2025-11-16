package extensions;

/**
 * Represents a single packet or flow unit in the NetworkQueue.
 * Stores arrival, service, and departure times.
 */
public class QueueElement {

    private final double arrivalTime;
    private double serviceStartTime = -1;
    private double departureTime = -1;

    /**
     * Creates a queue element that arrives at the specified time.
     *
     * @param arrivalTime timestamp when the packet reaches the queue
     */
    public QueueElement(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /** @return arrival time of the packet */
    public double getArrivalTime() { return arrivalTime; }

    /** @return moment when service begins, or -1 if not yet started */
    public double getServiceStartTime() { return serviceStartTime; }

    /** Records the moment service begins. */
    public void setServiceStartTime(double t) { this.serviceStartTime = t; }

    /** @return departure time, or -1 if still in the system */
    public double getDepartureTime() { return departureTime; }

    /** Records when the packet leaves the queueing system. */
    public void setDepartureTime(double t) { this.departureTime = t; }

    /** @return total time spent in the system (service + waiting) */
    public double getTotalDelay() {
        if (departureTime < 0) return 0;
        return departureTime - arrivalTime;
    }

    /** @return waiting time before service begins */
    public double getWaitingTime() {
        if (serviceStartTime < 0) return 0;
        return serviceStartTime - arrivalTime;
    }
}
