package edu.ucumberlands.dps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for the Java implementation of the Data Processing System.
 *
 * Responsibilities:
 *  - Create a shared task queue.
 *  - Create a shared, thread-safe results list.
 *  - Start multiple worker threads using an ExecutorService.
 *  - Add normal tasks to the queue.
 *  - Add poison pills to signal workers to stop.
 *  - Wait for all workers to finish.
 *  - Write the results to an output file.
 */
public class Main {

    public static void main(String[] args) {

        // Configuration: how many workers and tasks to use
        int numWorkers = 4;
        int numTasks = 10;
        String outputFile = "java_results.txt";

        System.out.println("Starting Data Processing System in Java...");
        System.out.printf("Number of workers: %d, number of tasks: %d%n", numWorkers, numTasks);

        // Create the shared task queue
        SharedTaskQueue queue = new SharedTaskQueue();

        // Create a shared results list.
        // Collections.synchronizedList makes the list thread-safe for multiple writers.
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Create a fixed-size thread pool to manage worker threads
        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);

        // Start worker threads
        for (int i = 1; i <= numWorkers; i++) {
            executor.submit(new Worker(i, queue, results));
        }

        // Add normal tasks to the queue
        for (int i = 1; i <= numTasks; i++) {
            String data = "task_data_" + i;
            Task task = new Task(i, data);

            try {
                queue.addTask(task);
                System.out.printf("Main thread added Task-%d (%s) to the queue.%n", i, data);
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while adding tasks: " + e.getMessage());
                Thread.currentThread().interrupt(); // restore interrupt status
                break; // stop adding more tasks if interrupted
            }
        }

        // Add one poison pill per worker for graceful shutdown
        System.out.println("Main thread adding poison pills to the queue...");
        for (int i = 0; i < numWorkers; i++) {
            try {
                queue.addTask(Task.POISON_PILL);
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while adding poison pills: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Tell the executor that no new tasks will be submitted
        executor.shutdown();

        try {
            // Wait up to 60 seconds for workers to finish
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Timed out waiting for worker threads to finish.");
            }
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted while waiting for executor: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        // After workers are done, write results to the output file
        System.out.println("Writing results to file: " + outputFile);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            synchronized (results) {
                for (String line : results) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            System.out.println("Results successfully written to " + outputFile);
        } catch (IOException e) {
            System.err.println("Error writing results to file: " + e.getMessage());
        }

        System.out.println("Java Data Processing System finished.");
    }
}
