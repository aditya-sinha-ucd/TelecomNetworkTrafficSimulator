/**
 * @file src/extensions/QueueElement.java
 * @brief Data container for individual packets in {@link extensions.NetworkQueue}.
 * @details Stores arrival, service start, and departure timestamps so that the
 *          queue can compute waiting and system times. Collaborates exclusively
 *          with {@link NetworkQueue}, which mutates the service/departure times
 *          as service progresses.
 * @date 2024-05-30
 */
package extensions;

/**
 * @class QueueElement
 * @brief Represents a single packet or flow unit in the queue.
 * @details Acts as a mutable record used during deterministic service
 *          calculations. Inputs: arrival time via constructor; outputs: derived
 *          delays queried by {@link NetworkQueue}.
 */
public class QueueElement {

    private final double arrivalTime;
    private double serviceStartTime = -1;
    private double departureTime = -1;

    /**
     * @brief Creates a queue element that arrives at the specified time.
     * @param arrivalTime Timestamp when the packet reaches the queue.
     */
    public QueueElement(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * @brief Arrival time of the packet.
     * @return Timestamp provided at construction.
     */
    public double getArrivalTime() { return arrivalTime; }

    /**
     * @brief Moment when service begins, or -1 if not yet started.
     * @return Service start timestamp.
     */
    public double getServiceStartTime() { return serviceStartTime; }

    /**
     * @brief Records the moment service begins.
     * @param t Service start timestamp.
     */
    public void setServiceStartTime(double t) { this.serviceStartTime = t; }

    /**
     * @brief Departure time, or -1 if still in the system.
     * @return Service completion timestamp.
     */
    public double getDepartureTime() { return departureTime; }

    /**
     * @brief Records when the packet leaves the queueing system.
     * @param t Departure timestamp.
     */
    public void setDepartureTime(double t) { this.departureTime = t; }

    /**
     * @brief Total time spent in the system (waiting + service).
     * @return Sojourn time in seconds; zero if departure not set.
     */
    public double getTotalDelay() {
        if (departureTime < 0) return 0;
        return departureTime - arrivalTime;
    }

    /**
     * @brief Waiting time before service begins.
     * @return Waiting duration; zero if service not started.
     */
    public double getWaitingTime() {
        if (serviceStartTime < 0) return 0;
        return serviceStartTime - arrivalTime;
    }
}
