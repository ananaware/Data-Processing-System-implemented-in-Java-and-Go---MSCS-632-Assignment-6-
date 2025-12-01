package edu.ucumberlands.dps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * SharedTaskQueue represents a thread-safe queue of tasks that can be safely
 * accessed by multiple producer and consumer threads.
 *
 * In this assignment:
 * - The main thread acts as a producer and adds Task objects to the queue.
 * - Worker threads act as consumers and retrieve Task objects from the queue.
 *
 * This class uses a BlockingQueue implementation (LinkedBlockingQueue)
 * to automatically handle synchronization and waiting when the queue is empty.
 */
public class SharedTaskQueue {

    // Internal queue that holds Task objects. BlockingQueue is thread-safe.
    private final BlockingQueue<Task> queue;

    /**
     * Creates a new SharedTaskQueue backed by a LinkedBlockingQueue.
     */
    public SharedTaskQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds a Task to the queue. If the queue is full (which is unlikely in this
     * simple example), this call may block until space is available.
     *
     * @param task the Task to add
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void addTask(Task task) throws InterruptedException {
        queue.put(task);  // may block and throw InterruptedException
    }

    /**
     * Retrieves and removes the next Task from the queue, waiting if necessary
     * until a Task becomes available.
     *
     * @return the next Task from the queue
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public Task getTask() throws InterruptedException {
        return queue.take(); // waits if queue is empty
    }

    /**
     * Returns the current number of tasks in the queue.
     * This is mainly useful for logging or debugging.
     *
     * @return the size of the queue
     */
    public int size() {
        return queue.size();
    }
}
