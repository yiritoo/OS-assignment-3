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

        // Sort processes by arrival time to ensure correct initial order
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        // Reset process state for the simulation
        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
            p.setCompletionTime(0);
        }

        Queue<Process> readyQueue = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();

        int currentTime = 0;
        int completedProcesses = 0;
        int n = processes.size();
        int processIndex = 0;
        Process lastRanProcess = null;

        while (completedProcesses < n) {

            // 1. Check for new arrivals at the current time
            while (processIndex < n && processes.get(processIndex).getArrivalTime() <= currentTime) {
                readyQueue.add(processes.get(processIndex));
                processIndex++;
            }

            // 2. If queue is empty, jump time to the next process arrival
            if (readyQueue.isEmpty()) {
                if (processIndex < n) {
                    currentTime = processes.get(processIndex).getArrivalTime();
                    // Add the process(es) that just arrived
                    while (processIndex < n && processes.get(processIndex).getArrivalTime() <= currentTime) {
                        readyQueue.add(processes.get(processIndex));
                        processIndex++;
                    }
                }
                continue;
            }

            Process currentProcess = readyQueue.poll();

            // 3. Handle Context Switching
            // We loop through the context switch time to catch any processes arriving DURING the switch
            if (lastRanProcess != null && currentProcess != lastRanProcess) {
                for (int i = 0; i < contextSwitchTime; i++) {
                    currentTime++;
                    while (processIndex < n && processes.get(processIndex).getArrivalTime() <= currentTime) {
                        readyQueue.add(processes.get(processIndex));
                        processIndex++;
                    }
                }
            }

            // 4. Execute the Process
            executionOrder.add(currentProcess.getName());

            int timeSlice = Math.min(this.quantum, currentProcess.getRemainingBurstTime());

            for (int t = 0; t < timeSlice; t++) {
                currentTime++;
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() - 1);

                // Check for arrivals during execution
                while (processIndex < n && processes.get(processIndex).getArrivalTime() <= currentTime) {
                    readyQueue.add(processes.get(processIndex));
                    processIndex++;
                }
            }

            // 5. Completion or Re-queue
            if (currentProcess.getRemainingBurstTime() > 0) {
                readyQueue.add(currentProcess);
            } else {
                completedProcesses++;
                currentProcess.setCompletionTime(currentTime);
            }

            lastRanProcess = currentProcess;
        }

        report.executionOrder = executionOrder;
        calculateMetrics(report);

        return report;
    }

    private void calculateMetrics(ScheduleResult result) {
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;

        for (Process p : result.processes) {
            int turnAround = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(turnAround);

            int waiting = turnAround - p.getTotalBurstTime();
            if (waiting < 0) waiting = 0;
            p.setWaitingTime(waiting);

            totalTurnaroundTime += turnAround;
            totalWaitingTime += waiting;
        }

        if (!result.processes.isEmpty()) {
            result.avgTurnaroundTime = totalTurnaroundTime / result.processes.size();
            result.avgWaitingTime = totalWaitingTime / result.processes.size();
        } else {
            result.avgTurnaroundTime = 0;
            result.avgWaitingTime = 0;
        }
    }
}