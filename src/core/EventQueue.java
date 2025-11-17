/**
 * @file src/core/EventQueue.java
 * @brief Priority-queue backed scheduler used by the simulator core.
 * @details Encapsulates a {@link java.util.PriorityQueue} of {@link core.Event}
 *          instances ordered by time. The queue is the primary collaborator of
 *          {@link core.Simulator}, supplying it with the next chronological
 *          event to execute. Inputs are {@link Event} objects produced by
 *          traffic sources, and outputs are the same events delivered in sorted
 *          order for processing.
 * @date 2024-05-30
 */
package core;

import java.util.PriorityQueue;

/**
 * @class EventQueue
 * @brief Thin wrapper around {@link PriorityQueue} tailored for simulation.
 * @details Responsible for lifecycle management of scheduled events, providing
 *          operations to enqueue, poll, peek, and clear events. The class also
 *          exposes diagnostic information such as size for telemetry.
 */
public class EventQueue {

    /** Internal priority queue storing events sorted by timestamp. */
    private final PriorityQueue<Event> queue;

    /**
     * @brief Constructs an empty event queue ready for scheduling.
     */
    public EventQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * @brief Adds a new event to the scheduler.
     * @param event {@link Event} to be ordered chronologically.
     */
    public void addEvent(Event event) {
        queue.add(event);
    }

    /**
     * @brief Retrieves and removes the earliest event.
     * @return Next {@link Event} to execute, or {@code null} if no events
     *         remain in the queue.
     */
    public Event nextEvent() {
        return queue.poll();
    }

    /**
     * @brief Peeks at the next event without dequeuing it.
     * @return Upcoming {@link Event} or {@code null} when empty.
     */
    public Event peekNextEvent() {
        return queue.peek();
    }

    /**
     * @brief Indicates whether there are events left to process.
     * @return {@code true} if the queue currently holds no events.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * @brief Reports the number of events waiting to be processed.
     * @return Count of scheduled events.
     */
    public int size() {
        return queue.size();
    }

    /**
     * @brief Removes all pending events, effectively resetting scheduling.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * @brief Describes the current queue state for logging.
     * @return String containing the queue size summary.
     */
    @Override
    public String toString() {
        return "EventQueue{size=" + queue.size() + "}";
    }
}
