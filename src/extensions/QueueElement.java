package extensions;

/**
 * Represents a single packet or flow unit in the NetworkQueue.
 * Stores arrival, service, and departure times.
 */
public class QueueElement {

    private final double arrivalTime;
    private double serviceStartTime = -1;
    private double departureTime = -1;

    public QueueElement(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getArrivalTime() { return arrivalTime; }
    public double getServiceStartTime() { return serviceStartTime; }
    public void setServiceStartTime(double t) { this.serviceStartTime = t; }
    public double getDepartureTime() { return departureTime; }
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
