package Scheduler;

import model.Process;
import java.util.*;

public class SJFScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        ScheduleResult result = new ScheduleResult();

        // Work on a copy of input to avoid mutating it
        List<Process> processes = Process.copyList(input);
        result.processes = processes;

        // FIXED: Used Setters and correct naming from Process.java
        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
        }

        int n = processes.size();
        int finished = 0;
        int time = 0;

        Process current = null;

        while (finished < n) {
            Process next = pickShortestRemaining(processes, time);

            // Idle CPU
            if (next == null) {
                time += 1;
                current = null;
                continue;
            }

            // Context switch logic
            if (current != null && current != next) {
                if (contextSwitchTime > 0) {
                    time += contextSwitchTime;
                }
            }

            // After context switch, pick again
            next = pickShortestRemaining(processes, time);
            if (next == null) {
                time += 1;
                current = null;
                continue;
            }
            current = next;

            // FIXED: Used Getter/Setter for remaining time
            current.setRemainingBurstTime(current.getRemainingBurstTime() - 1);
            time += 1;

            // If process finished, record stats
            if (current.getRemainingBurstTime() == 0) {
                finished++;

                // FIXED: Used Setters and Getters for metrics
                int completionTime = time;
                int tat = completionTime - current.getArrivalTime();
                current.setTurnaroundTime(tat);
                current.setWaitingTime(tat - current.getTotalBurstTime());
                current.setCompletionTime(completionTime); // Added to track finish time
                current = null;
            }
        }
        getAverages(result);

        return result;
    }

    private Process pickShortestRemaining(List<Process> processes, int time) {
        Process shortest = null;

        for (Process p : processes) {
            // FIXED: Replaced all .remainingTime and .arrivalTime with Getters
            if (p.getArrivalTime() <= time && p.getRemainingBurstTime() > 0) {
                if (shortest == null) {
                    shortest = p;
                } else if (p.getRemainingBurstTime() < shortest.getRemainingBurstTime()) {
                    shortest = p;
                } else if (p.getRemainingBurstTime() == shortest.getRemainingBurstTime()) {
                    // Tie-breakers: Arrival time first, then Name
                    if (p.getArrivalTime() < shortest.getArrivalTime()) {
                        shortest = p;
                    } else if (p.getArrivalTime() == shortest.getArrivalTime()) {
                        if (p.getName().compareTo(shortest.getName()) < 0)
                            shortest = p;
                    }
                }
            }
        }

        return shortest;
    }

    private void getAverages(ScheduleResult result) {
        double sumwaiting = 0;
        double sumturn = 0;

        // FIXED: Used Getters for average calculations
        for (Process p : result.processes) {
            sumwaiting += p.getWaitingTime();
            sumturn += p.getTurnaroundTime();
        }

        int n = result.processes.size();
        result.avgWaitingTime = (n == 0) ? 0 : (sumwaiting / n);
        result.avgTurnaroundTime = (n == 0) ? 0 : (sumturn / n);
    }
}