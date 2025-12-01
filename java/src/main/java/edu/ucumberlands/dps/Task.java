package edu.ucumberlands.dps;

/**
 * Represents a unit of work in the Data Processing System.
 * Each Task has an ID and some text data to process.
 * A special Task with id = -1 is used as a "poison pill" to signal workers to stop.
 */
public class Task {

    // Unique ID for the task (1, 2, 3, ... or -1 for poison pill)
    private final int id;

    // The text data that will be processed by worker threads
    private final String data;

    // A static constant Task instance that represents the poison pill
    public static final Task POISON_PILL = new Task(-1, "POISON");

    /**
     * Creates a new Task with the given id and data.
     *
     * @param id   the numeric ID of the task
     * @param data the text data associated with this task
     */
    public Task(int id, String data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Returns the ID of this task.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the text data for this task.
     */
    public String getData() {
        return data;
    }

    /**
     * Returns true if this task is the special poison pill task.
     * Workers will use this to decide when to stop.
     */
    public boolean isPoisonPill() {
        return this.id == -1;
    }
}
