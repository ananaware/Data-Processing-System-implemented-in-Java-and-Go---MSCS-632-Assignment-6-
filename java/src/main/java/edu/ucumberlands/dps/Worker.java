package edu.ucumberlands.dps;

import java.util.List;

/**
 * Worker represents a single worker thread in the Data Processing System.
 * Each Worker repeatedly:
 *  - retrieves a Task from the shared queue,
 *  - processes the Task (simulate work, transform data),
 *  - writes a result message to a shared results list,
 *  - logs its activity (start, processing, errors, completion).
 *
 * When the Worker receives a poison pill task, it will stop running.
 */
public class Worker implements Runnable {

    // ID number for this worker (1, 2, 3, ...)
    private final int workerId;

    // Shared queue from which this worker gets tasks
    private final SharedTaskQueue queue;

    // Shared list where this worker stores results after processing tasks
    private final List<String> results;

    /**
     * Creates a new Worker.
     *
     * @param workerId a unique ID for the worker (used only for logging)
     * @param queue    the shared task queue
     * @param results  the shared results list (must be synchronized externally)
     */
    public Worker(int workerId, SharedTaskQueue queue, List<String> results) {
        this.workerId = workerId;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public void run() {
        System.out.printf("Worker-%d started.%n", workerId);

        try {
            while (true) {
                // Get the next task from the shared queue (waits if empty)
                Task task = queue.getTask(); // may throw InterruptedException

                // Check for poison pill to stop the worker
                if (task.isPoisonPill()) {
                    System.out.printf("Worker-%d received poison pill. Shutting down.%n", workerId);
                    break;
                }

                // Log that this worker is about to process the task
                System.out.printf("Worker-%d processing Task-%d%n", workerId, task.getId());

                try {
                    // Simulate computational work with a random delay (200–500 ms)
                    long delayMillis = 200 + (long) (Math.random() * 300);
                    Thread.sleep(delayMillis);

                    // Processing logic: convert data to upper case and get its length
                    String input = task.getData();
                    String output = input.toUpperCase();
                    int length = output.length();

                    // Build a result message
                    String resultLine = String.format(
                            "Worker-%d processed Task-%d: \"%s\" -> \"%s\" (len=%d, delay=%dms)",
                            workerId, task.getId(), input, output, length, delayMillis
                    );

                    // Synchronize access to the shared results list
                    synchronized (results) {
                        results.add(resultLine);
                    }

                    // Log the successful processing
                    System.out.println(resultLine);

                } catch (InterruptedException e) {
                    // This worker was interrupted during sleep or processing
                    System.err.printf("Worker-%d interrupted during processing: %s%n",
                            workerId, e.getMessage());
                    Thread.currentThread().interrupt(); // restore interrupt status
                    break; // exit the loop and stop this worker

                } catch (Exception e) {
                    // Any other unexpected error during processing
                    System.err.printf("Worker-%d encountered an error while processing Task-%d: %s%n",
                            workerId, task.getId(), e.getMessage());
                    // In a real system, we might also log a stack trace here
                }
            }
        } catch (InterruptedException e) {
            // This happens if the worker is interrupted while waiting for a task
            System.err.printf("Worker-%d interrupted while waiting for tasks: %s%n",
                    workerId, e.getMessage());
            Thread.currentThread().interrupt(); // restore interrupt status
        }

        System.out.printf("Worker-%d completed.%n", workerId);
    }
}
