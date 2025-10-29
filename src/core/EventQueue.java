package core;

import java.util.PriorityQueue;

/**
 * Manages the event scheduling and ordering for the simulation.
 * <p>
 * This class acts as the central mechanism that determines
 * which event occurs next. It uses a {@link PriorityQueue}
 * to ensure events are always processed in chronological order.
 */
public class EventQueue {

    /* Internal priority queue that stores events sorted by time. */
    private final PriorityQueue<Event> queue;

    // Constructs an empty event queue.
    public EventQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Adds a new event to the queue.
     *
     * @param event the event to schedule
     */
    public void addEvent(Event event) {
        queue.add(event);
    }

    /*
     * Retrieves and removes the next (earliest) event from the queue.
     * @return the next scheduled event, or null if the queue is empty
     */

    public Event nextEvent() {
        return queue.poll();
    }

    /*
     * Peeks at the next event without removing it.
     * @return the next event in line, or null if the queue is empty
     */
    public Event peekNextEvent() {
        return queue.peek();
    }

    /*
     * Checks if there are any remaining events to process.
     * @return true if no events remain, false otherwise
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // @return the current number of events in the queue
    public int size() {
        return queue.size();
    }

    /*
     * Removes all events from the queue.
     * Useful when resetting or restarting the simulation.
     */
    public void clear() {
        queue.clear();
    }

    /*
     * Provides a string summary of the current queue state,
     * for debugging or status messages during the simulation.
     */
    @Override
    public String toString() {
        return "EventQueue{size=" + queue.size() + "}";
    }
}
