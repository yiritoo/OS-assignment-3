package Scheduler;

import model.Process;
import java.util.*;

public class RoundRobinScheduler implements Scheduler {

    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public ScheduleResult schedule(List<Process> processes, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();

        report.processes = processes;

        // 1. Setup: Sort processes by arrival time initially to manage flow
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        // Initialize dynamic fields (good practice to ensure clean state)
        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
            p.setCompletionTime(0);
            // Ensure started flag is reset if reusing objects
        }

        Queue<Process> readyQueue = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();

        int currentTime = 0;
        int completedProcesses = 0;
        int n = processes.size();
        int processIndex = 0; // To track which processes from the main list have arrived

        Process lastRanProcess = null;

        // 2. Main Simulation Loop
        while (completedProcesses < n) {

            // Check for new arrivals at the exact start of this iteration
            while (processIndex < n && processes.get(processIndex).getArrivalTime() <= currentTime) {
                readyQueue.add(processes.get(processIndex));
                processIndex++;
            }

            // If queue is empty, jump time to next arrival
            if (readyQueue.isEmpty()) {
                if (processIndex < n) {
                    currentTime = processes.get(processIndex).getArrivalTime();
                    // Don't add to queue here, let the loop restart and add it in the while check above
                }
                continue;
            }

            Process currentProcess = readyQueue.poll();

            // 3. Context Switch Logic
            // Apply context switch if we are changing processes (and it's not the very first run)
            if (lastRanProcess != null && currentProcess != lastRanProcess) {
                currentTime += contextSwitchTime;
            }

            // Record execution
            executionOrder.add(currentProcess.getName());

            // 4. Execution Logic
            // Determine how long to run: either the full quantum or the remaining burst
            int timeSlice = Math.min(this.quantum, currentProcess.getRemainingBurstTime());

            // "Run" the process
            currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() - timeSlice);

            // Update the global time
            // We must also check for arrivals that happen *during* this time slice
            for (int t = 0; t < timeSlice; t++) {
                currentTime++;
                // Check if any process arrives at this specific tick
                while (processIndex < n && processes.get(processIndex).getArrivalTime() == currentTime) {
                    readyQueue.add(processes.get(processIndex));
                    processIndex++;
                }
            }

            // 5. Completion or Re-queue
            if (currentProcess.getRemainingBurstTime() > 0) {
                // Not finished: Add back to the end of the queue
                readyQueue.add(currentProcess);
            } else {
                // Finished
                completedProcesses++;
                currentProcess.setCompletionTime(currentTime);
            }

            lastRanProcess = currentProcess;
        }

        report.executionOrder = executionOrder;

        // 6. Calculate Output Metrics (Mohamed's Responsibility)
        calculateMetrics(report);

        return report;
    }

    /**
     * Calculates Waiting Time, Turnaround Time, and Averages.
     */
    private void calculateMetrics(ScheduleResult result) {
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;

        for (Process p : result.processes) {
            // Turnaround Time = Completion Time - Arrival Time
            int turnAround = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(turnAround);

            // Waiting Time = Turnaround Time - Burst Time
            int waiting = turnAround - p.getTotalBurstTime();
            // Sanity check: waiting time cannot be negative
            if (waiting < 0) waiting = 0;
            p.setWaitingTime(waiting);

            totalTurnaroundTime += turnAround;
            totalWaitingTime += waiting;
        }

        // Calculate Averages
        if (!result.processes.isEmpty()) {
            result.avgTurnaroundTime = totalTurnaroundTime / result.processes.size();
            result.avgWaitingTime = totalWaitingTime / result.processes.size();
        } else {
            result.avgTurnaroundTime = 0;
            result.avgWaitingTime = 0;
        }
    }
}