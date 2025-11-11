package extensions;

public class QueueElement {
    public final double arrivalTime;
    public double serviceStartTime = -1;
    public double departureTime = -1;

    public QueueElement(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double totalTimeInSystem() {
        return (departureTime < 0) ? -1 : (departureTime - arrivalTime);
    }

    public double waitingTime() {
        return (serviceStartTime < 0) ? -1 : (serviceStartTime - arrivalTime);
    }
}
