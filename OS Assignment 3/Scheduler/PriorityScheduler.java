package Scheduler;

import model.Process;
import java.util.*;

public class PriorityScheduler implements Scheduler {

    private int agingInterval = 5;

    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        return schedule(input, contextSwitchTime, 5);
    }

    public ScheduleResult schedule(List<Process> input, int contextSwitchTime, int agingInterval) {
        this.agingInterval = agingInterval;

        ScheduleResult result = new ScheduleResult();
        List<Process> processes = Process.copyList(input);
        result.processes = processes;

        // Initialize
        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setDynamicPriority(p.getInitialPriority());
            p.setStartTime(-1);
            p.setLastUpdate(p.getArrivalTime());
            p.setPid(i);
        }

        int time = 0;
        int completed = 0;
        Process running = null;

        while (completed < processes.size()) {

            // Apply aging to waiting processes
            for (Process p : processes) {
                if (p.getRemainingBurstTime() > 0 &&
                        p.getArrivalTime() <= time &&
                        p != running) {

                    int waitTime = time - p.getLastUpdate();
                    if (waitTime > 0 && waitTime % agingInterval == 0) {
                        p.setDynamicPriority(Math.max(1, p.getDynamicPriority() - 1));
                        p.setLastUpdate(time);
                    }
                }
            }

            // Pick highest priority
            Process next = pickHighestPriority(processes, time);

            // CPU Idle
            if (next == null) {
                time++;
                running = null;
                continue;
            }

            // Determine if context switch is needed
            boolean needsContextSwitch = false;

            if (running == null) {
                // No process was running (idle or first process or after completion)
                result.executionOrder.add(next.getName());
                running = next;
            } else if (running != next) {
                // Different process selected - need context switch
                needsContextSwitch = true;
                result.executionOrder.add(next.getName());

                // Update lastUpdate for the process being preempted
                running.setLastUpdate(time);

                // Perform context switch
                time += contextSwitchTime;

                // Apply aging after context switch
                for (Process p : processes) {
                    if (p.getRemainingBurstTime() > 0 &&
                            p.getArrivalTime() <= time &&
                            p != next) {

                        int waitTime = time - p.getLastUpdate();
                        if (waitTime > 0 && waitTime % agingInterval == 0) {
                            p.setDynamicPriority(Math.max(1, p.getDynamicPriority() - 1));
                            p.setLastUpdate(time);
                        }
                    }
                }

                running = next;
            }
            // else: same process continues, no action needed

            // Set start time on first execution
            if (running.getStartTime() == -1) {
                running.setStartTime(time);
            }

            // Execute one time unit
            running.execute(1);
            time++;

            // Update lastUpdate after execution
            running.setLastUpdate(time);

            // Check for completion
            if (running.isCompleted()) {
                running.setCompletionTime(time);
                completed++;
                running = null; // Clear running so next process starts without CS
            }
        }

        // Calculate metrics
        for (Process p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getTotalBurstTime());
        }

        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            totalWT += p.getWaitingTime();
            totalTAT += p.getTurnaroundTime();
        }

        result.avgWaitingTime = totalWT / processes.size();
        result.avgTurnaroundTime = totalTAT / processes.size();

        return result;
    }

    private Process pickHighestPriority(List<Process> processes, int time) {
        Process best = null;

        for (Process p : processes) {
            if (p.getArrivalTime() <= time && p.getRemainingBurstTime() > 0) {
                if (best == null) {
                    best = p;
                } else {
                    if (p.getDynamicPriority() < best.getDynamicPriority()) {
                        best = p;
                    } else if (p.getDynamicPriority() == best.getDynamicPriority()) {
                        if (p.getArrivalTime() < best.getArrivalTime()) {
                            best = p;
                        } else if (p.getArrivalTime() == best.getArrivalTime()) {
                            if (p.getPid() < best.getPid()) {
                                best = p;
                            }
                        }
                    }
                }
            }
        }

        return best;
    }
}