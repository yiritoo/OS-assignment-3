package Scheduler;

import model.Process;
import java.util.*;

public class PriorityScheduler implements Scheduler {
    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();
        List<Process> processes = Process.copyList(input);
        report.processes = processes;

        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setDynamicPriority(p.getInitialPriority());
            p.setStartTime(-1);
        }

        int currentTime = 0;
        int completed = 0;
        Process lastProcess = null;

        while (completed < processes.size()) {

            applyAging(processes, currentTime);

            Process current = pickHighestPriority(processes, currentTime);

            if (current == null) {
                currentTime++;
                continue;
            }

            if (lastProcess != null && lastProcess != current && lastProcess.getRemainingBurstTime() > 0) {
                for (int i = 0; i < contextSwitchTime; i++) {
                    currentTime++;
                    applyAging(processes, currentTime);
                }
                current = pickHighestPriority(processes, currentTime);
                if (current == null) continue;
            }

            if (!current.hasStarted()) {
                current.setStartTime(currentTime);
            }

            report.executionOrder.add(current.getName());
            current.execute(1);
            currentTime++;

            if (current.isCompleted()) {
                completed++;
                current.setCompletionTime(currentTime);
            }
            lastProcess = current;
        }
        calculateMetrics(report);
        return report;
    }

    private void applyAging(List<Process> processes, int currentTime) {

        if (currentTime > 0 && currentTime % 5 == 0) {
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && p.getRemainingBurstTime() > 0) {
                    p.setDynamicPriority(Math.max(1, p.getDynamicPriority() - 1));
                }
            }
        }
    }

    private Process pickHighestPriority(List<Process> processes, int currentTime) {
        Process best = null;
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && p.getRemainingBurstTime() > 0) {
                if (best == null) {
                    best = p;
                } else if (p.getDynamicPriority() < best.getDynamicPriority()) {
                    best = p;
                } else if (p.getDynamicPriority() == best.getDynamicPriority()) {
                    // Tie-breaker: Arrival Time (FCFS)
                    if (p.getArrivalTime() < best.getArrivalTime()) {
                        best = p;
                    }
                }
            }
        }
        return best;
    }

    private void calculateMetrics(ScheduleResult result) {
        double totalWT = 0, totalTAT = 0;
        for (Process p : result.processes) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt = tat - p.getTotalBurstTime();
            if (wt < 0) wt = 0;

            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
            totalTAT += tat;
            totalWT += wt;
        }
        if (!result.processes.isEmpty()) {
            result.avgTurnaroundTime = totalTAT / result.processes.size();
            result.avgWaitingTime = totalWT / result.processes.size();
        }
    }
}