package main

import (
    "bufio"
    "fmt"
    "os"
    "strings"
    "sync"
    "time"
)

// Task represents a unit of work in the Go Data Processing System.
// It has an ID and a piece of text data to process.
type Task struct {
    ID   int
    Data string
}

// PoisonPillID is the special ID used to signal workers to stop.
const PoisonPillID = -1

// worker is a goroutine function that:
//
//   - reads Task values from the tasks channel,
//   - simulates processing (sleep),
//   - transforms the data (to upper case, compute length),
//   - appends a result string to the shared results slice,
//   - logs its activity.
//
// When it receives a Task with ID == PoisonPillID, it logs a shutdown
// message and returns, which decrements the WaitGroup counter.
func worker(workerID int, tasks <-chan Task, results *[]string, mu *sync.Mutex, wg *sync.WaitGroup) {
    defer wg.Done()

    fmt.Printf("Worker-%d started.\n", workerID)

    for task := range tasks {
        // Check for poison pill
        if task.ID == PoisonPillID {
            fmt.Printf("Worker-%d received poison pill. Shutting down.\n", workerID)
            break
        }

        fmt.Printf("Worker-%d processing Task-%d\n", workerID, task.ID)

        // Simulate computational work with a random delay between 200–500 ms
        delay := 200 + time.Duration(time.Now().UnixNano()%300)
        time.Sleep(delay * time.Millisecond)

        // Processing: uppercase the data and get its length
        input := task.Data
        output := strings.ToUpper(input)
        length := len(output)

        // Build result line
        resultLine := fmt.Sprintf(
            "Worker-%d processed Task-%d: %q -> %q (len=%d, delay=%dms)",
            workerID, task.ID, input, output, length, delay,
        )

        // Append to shared results slice safely
        mu.Lock()
        *results = append(*results, resultLine)
        mu.Unlock()

        // Log success
        fmt.Println(resultLine)
    }

    fmt.Printf("Worker-%d completed.\n", workerID)
}

func main() {
    // Configuration
    numWorkers := 4
    numTasks := 10
    outputFile := "go_results.txt"

    fmt.Println("Starting Data Processing System in Go...")
    fmt.Printf("Number of workers: %d, number of tasks: %d\n", numWorkers, numTasks)

    // Channel acts as our thread-safe task queue
    tasks := make(chan Task)

    // Shared results slice + mutex for safe concurrent access
    var results []string
    var mu sync.Mutex

    // WaitGroup to wait for all workers to finish
    var wg sync.WaitGroup
    wg.Add(numWorkers)

    // Start worker goroutines
    for i := 1; i <= numWorkers; i++ {
        go worker(i, tasks, &results, &mu, &wg)
    }

    // Producer: add normal tasks to the channel
    for i := 1; i <= numTasks; i++ {
        data := fmt.Sprintf("task_data_%d", i)
        task := Task{ID: i, Data: data}
        fmt.Printf("Main goroutine adding Task-%d (%s) to the channel.\n", i, data)
        tasks <- task
    }

    // Add one poison pill per worker
    fmt.Println("Main goroutine adding poison pills to the channel...")
    for i := 0; i < numWorkers; i++ {
        tasks <- Task{ID: PoisonPillID, Data: "POISON"}
    }

    // We can close the channel after sending all tasks + poison pills.
    close(tasks)

    // Wait for all workers to finish
    wg.Wait()

    // Write results to file
    fmt.Printf("Writing results to file: %s\n", outputFile)
    if err := writeResultsToFile(outputFile, results); err != nil {
        fmt.Printf("Error writing results to file: %v\n", err)
    } else {
        fmt.Printf("Results successfully written to %s\n", outputFile)
    }

    fmt.Println("Go Data Processing System finished.")
}

// writeResultsToFile writes all result lines to the given file,
// one line per result. It demonstrates Go-style error handling:
// functions return 'error' and the caller checks 'if err != nil'.
func writeResultsToFile(filename string, results []string) error {
    file, err := os.Create(filename)
    if err != nil {
        return err
    }
    defer file.Close()

    writer := bufio.NewWriter(file)
    for _, line := range results {
        if _, err := writer.WriteString(line + "\n"); err != nil {
            return err
        }
    }

    if err := writer.Flush(); err != nil {
        return err
    }

    return nil
}
