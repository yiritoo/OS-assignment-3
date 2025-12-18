package Scheduler;
import model.Process;
import java.util.*;

public class PriorityScheduler implements Scheduler {
    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();
        List<Process> processes = Process.copyList(input);
        report.processes = processes;

        int currentTime = 0;
        int completed = 0;
        Process lastProcess = null;

        while (completed < processes.size()) {
            // Starvation Solution (Aging): Improve priority every 5 seconds of waiting
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && p.getRemainingBurstTime() > 0) {
                    if (currentTime > 0 && currentTime % 5 == 0) {
                        p.setDynamicPriority(Math.max(1, p.getDynamicPriority() - 1));
                    }
                }
            }

            // Pick highest priority (lowest number)
            Process current = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && p.getRemainingBurstTime() > 0) {
                    if (current == null || p.getDynamicPriority() < current.getDynamicPriority()) {
                        current = p;
                    }
                }
            }

            if (current == null) {
                currentTime++;
                continue;
            }

            // Apply context switch if we are changing processes
            if (lastProcess != null && lastProcess != current && lastProcess.getRemainingBurstTime() > 0) {
                currentTime += contextSwitchTime;
            }

            report.executionOrder.add(current.getName());
            current.execute(1); // Execute for 1 unit (Preemptive)
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

    private void calculateMetrics(ScheduleResult result) {
        double totalWT = 0, totalTAT = 0;
        for (Process p : result.processes) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt = tat - p.getTotalBurstTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
            totalTAT += tat;
            totalWT += wt;
        }
        result.avgTurnaroundTime = totalTAT / result.processes.size();
        result.avgWaitingTime = totalWT / result.processes.size();
    }
}
