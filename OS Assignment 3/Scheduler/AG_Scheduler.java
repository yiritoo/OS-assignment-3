import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.lang.Math;

public class AG_Scheduler {

    private final List<Process> allProcesses;

    // The Ready Queue prioritizes based on the current highest priority
    private final PriorityQueue<Process> readyQueue;

    // Constructor
    public AG_Scheduler(List<Process> processes) {
        this.allProcesses = processes;
        this.readyQueue = new PriorityQueue<>(
                (p1, p2) -> Integer.compare(p1.getDynamicPriority(), p2.getDynamicPriority())
        );
    }

    // Calculate the time slice for Phase 1 & 2 which is 25 %  of the current quantum
    private int calculatePhaseSlice(int currentQuantum) {
        return (int) Math.ceil(currentQuantum * 0.25);
    }


    // Calculates the quantum increase amount for Scenario 2
    private int calculateScenarioTwoIncrease(int remainingQuantum) {
        return (int) Math.ceil(remainingQuantum / 2.0);
    }

    // Determine the new quantum (The 4 Scenarios)
    public void updateQuantum(Process process, int timeUsedInQuantum, int preemptionScenario) {

        int oldQuantum = process.getCurrentQuantum();
        int remainingQuantum = oldQuantum - timeUsedInQuantum;
        int newQuantum = 0;

        switch (preemptionScenario) {
            // Scenario 1: Used all quantum time
            case 1:
                newQuantum = oldQuantum + 2;
                break;

            // Scenario 2: Preempted in Phase 2 (Non-preemptive Priority)
            case 2:
                int increase2 = calculateScenarioTwoIncrease(remainingQuantum);
                newQuantum = oldQuantum + increase2;
                break;

            // Scenario 3: Preempted in Phase 3 (Preemptive SJF)
            case 3:
                newQuantum = oldQuantum + remainingQuantum;
                break;

            // Scenario 4: Job completed
            case 4:
                newQuantum = 0;
                break;

            default:
                System.err.println("Error: Unknown preemption scenario " + preemptionScenario + " for " + process.getName());
                return;
        }

        // Apply the new quantum and log the update (Required Output)
        process.setCurrentQuantum(newQuantum);
        process.logQuantumUpdate();
        System.out.println("DEBUG: " + process.getName() + " Q updated to " + newQuantum + " (Scenario " + preemptionScenario + ")");
    }

    // Main Simulation Function
    public void runAGScheduling(int contextSwitch) {
        int currentTime = 0;
        Process runningProcess = null;
        int completedCount = 0;

        while (completedCount < allProcesses.size()) {

            // 1- CHECK FOR ARRIVALS AND AGING (Basel's logic for aging goes here)
            // Add arriving processes to the readyQueue.

            // 2- PROCESS SELECTION / CONTEXT SWITCHING
            Process nextProcess = readyQueue.peek();

            // Handle Context Switch Overhead (Yassen's logic)
            if (runningProcess != nextProcess && nextProcess != null) {
                if (runningProcess != null) {
                    currentTime += contextSwitch;
                }
                runningProcess = nextProcess;
            }

            // 3- EXECUTION
            if (runningProcess != null && !runningProcess.isCompleted()) {

                // Set start time if this is the first execution
                if (!runningProcess.hasStarted()) {
                    runningProcess.setStartTime(currentTime);
                }

                // NOTE: The core logic requires a time-step loop (currentTime++)
                // to check for preemption in Phase 3 and track phase breaks (Q1, Q2) correctly.

                // Run for one unit and re-evaluate
                int timeToExecute = 1;

                runningProcess.execute(timeToExecute);
                currentTime += timeToExecute;
                // Q_used tracking must happen here

                // POST-EXECUTION CHECK
                if (runningProcess.isCompleted()) {
                    updateQuantum(runningProcess, 1, 4); // Scenario 4 (Completion)
                    readyQueue.remove(runningProcess);
                    completedCount++;
                    // Calculate and set metrics
                    runningProcess = null;
                }
                // Add checks for Scenario 1, 2, and 3 here.
            } else {
                // Idle time if ready queue is empty
                currentTime++;
            }
        }
    }
}