package extensions;

/**
 * Represents a single packet or flow unit in the NetworkQueue.
 * <p>
 * Stores timing information for arrival, service, and departure.
 */
public class QueueElement {

    private final double arrivalTime;
    private double serviceStartTime;
    private double departureTime;

    public QueueElement(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getServiceStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(double serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    /** @return total time spent in system */
    public double getTotalDelay() {
        return departureTime - arrivalTime;
    }

    /** @return waiting time before service begins */
    public double getWaitingTime() {
        return serviceStartTime - arrivalTime;
    }
}
