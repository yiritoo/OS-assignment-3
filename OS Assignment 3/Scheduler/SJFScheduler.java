package Scheduler;

import model.Process;
import java.util.*;

public class SJFScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        ScheduleResult result = new ScheduleResult();
        List<Process> processes = Process.copyList(input);
        result.processes = processes;

        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
            p.setCompletionTime(0);
            p.setStartTime(-1);
        }

        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        Process lastRanProcess = null;

        while (completed < n) {
            Process current = pickShortestRemaining(processes, currentTime);

            if (current == null) {
                currentTime++;
                continue;
            }

            if (lastRanProcess != null && current != lastRanProcess) {
                for (int i = 0; i < contextSwitchTime; i++) {
                    currentTime++;
                }
                current = pickShortestRemaining(processes, currentTime);
                if (current == null) continue;
            }

            if (!current.hasStarted()) {
                current.setStartTime(currentTime);
            }

            result.executionOrder.add(current.getName());
            current.execute(1);
            currentTime++;

            if (current.isCompleted()) {
                completed++;
                current.setCompletionTime(currentTime);
                current = null;
            }

            lastRanProcess = current;
        }

        calculateMetrics(result);
        return result;
    }

    private Process pickShortestRemaining(List<Process> processes, int time) {
        Process shortest = null;

        for (Process p : processes) {
            if (p.getArrivalTime() <= time && p.getRemainingBurstTime() > 0) {
                if (shortest == null) {
                    shortest = p;
                } else if (p.getRemainingBurstTime() < shortest.getRemainingBurstTime()) {
                    shortest = p;
                } else if (p.getRemainingBurstTime() == shortest.getRemainingBurstTime()) {
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

    private void calculateMetrics(ScheduleResult result) {
        double sumwaiting = 0;
        double sumturn = 0;

        for (Process p : result.processes) {
            int turnAround = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(turnAround);

            int waiting = turnAround - p.getTotalBurstTime();
            if (waiting < 0) waiting = 0;
            p.setWaitingTime(waiting);

            sumturn += turnAround;
            sumwaiting += waiting;
        }

        int n = result.processes.size();
        result.avgWaitingTime = (n == 0) ? 0 : (sumwaiting / n);
        result.avgTurnaroundTime = (n == 0) ? 0 : (sumturn / n);
    }
}