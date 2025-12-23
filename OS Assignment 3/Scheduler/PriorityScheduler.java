package Scheduler;

import model.Process;
import java.util.*;

public class PriorityScheduler implements Scheduler {

    private List<Process> processes;
    private PriorityQueue<Process> readyQueue;
    private int time = 0;
    private int completed = 0;
    private Process current = null;
    private String lastAddedToOrder = "";
    private Process lastExecutedProcess = null;

    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitchTime) {
        return schedule(input, contextSwitchTime, 5);
    }

    public ScheduleResult schedule(List<Process> input, int contextSwitchTime, int agingInterval) {
        ScheduleResult result = new ScheduleResult();
        this.processes = Process.copyList(input);
        result.processes = this.processes;

        // Reset state
        time = 0;
        completed = 0;
        current = null;
        lastExecutedProcess = null;
        lastAddedToOrder = "";

        // Initialize processes
        for (Process p : processes) {
            p.setRemainingBurstTime(p.getTotalBurstTime());
            p.setDynamicPriority(p.getInitialPriority());
            p.setStartTime(-1);
            p.setLastUpdate(p.getArrivalTime());
        }

        // Priority Queue: priority → arrival → name
        readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(Process::getDynamicPriority)
                        .thenComparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getName)
        );

        while (completed < processes.size()) {

            addArrivals();
            applyAging(agingInterval);

            // Preemption check
            if (current != null && !readyQueue.isEmpty()) {
                Process best = readyQueue.peek();
                if (shouldPreempt(best, current)) {
                    current.setLastUpdate(time);
                    readyQueue.add(current);
                    current = null;
                }
            }

            // Select process
            if (current == null) {
                if (readyQueue.isEmpty()) {
                    time++;
                    continue;
                }

                while (true) {
                    Process candidate = readyQueue.poll();

                    if (lastExecutedProcess != null && candidate != lastExecutedProcess) {
                        performContextSwitch(contextSwitchTime); // ❌ NO AGING HERE
                    }

                    if (!readyQueue.isEmpty() && shouldPreempt(readyQueue.peek(), candidate)) {
                        candidate.setLastUpdate(time);
                        readyQueue.add(candidate);
                        continue;
                    }

                    current = candidate;
                    if (current.getStartTime() == -1) {
                        current.setStartTime(time);
                    }
                    break;
                }
            }

            // Execute
            if (current != null) {
                recordExecution(current, result);

                current.execute(1);
                time++;
                lastExecutedProcess = current;

                if (current.isCompleted()) {
                    completed++;
                    current.setCompletionTime(time);
                    current.setTurnaroundTime(time - current.getArrivalTime());
                    current.setWaitingTime(
                            current.getTurnaroundTime() - current.getTotalBurstTime()
                    );
                    current = null;
                }
            }
        }

        calculateMetrics(result);
        return result;
    }

    // ================= Helpers =================

    private void addArrivals() {
        for (Process p : processes) {
            if (p.getArrivalTime() == time) {
                p.setLastUpdate(time);
                readyQueue.add(p);
            }
        }
    }

    // FIXED AGING
    private void applyAging(int agingInterval) {
        if (agingInterval <= 0) return;

        boolean queueChanged = false;

        for (Process p : readyQueue) {
            int waited = time - p.getLastUpdate();
            int agingSteps = waited / agingInterval;

            if (agingSteps > 0) {
                p.setDynamicPriority(Math.max(1,
                        p.getDynamicPriority() - agingSteps));
                p.setLastUpdate(time);
                queueChanged = true;
            }
        }

        if (queueChanged) rebuildQueue();
    }

    // ✅ FIXED: NO AGING DURING CS
    private void performContextSwitch(int csTime) {
        for (int i = 0; i < csTime; i++) {
            time++;
            addArrivals();
        }
    }

    private boolean shouldPreempt(Process best, Process running) {
        if (best.getDynamicPriority() < running.getDynamicPriority()) return true;

        if (best.getDynamicPriority() == running.getDynamicPriority()) {
            if (best.getArrivalTime() < running.getArrivalTime()) return true;
            if (best.getArrivalTime() == running.getArrivalTime()) {
                return best.getName().compareTo(running.getName()) < 0;
            }
        }
        return false;
    }

    private void rebuildQueue() {
        List<Process> temp = new ArrayList<>(readyQueue);
        readyQueue.clear();
        readyQueue.addAll(temp);
    }

    private void recordExecution(Process p, ScheduleResult result) {
        if (!p.getName().equals(lastAddedToOrder)) {
            result.executionOrder.add(p.getName());
            lastAddedToOrder = p.getName();
        }
    }

    private void calculateMetrics(ScheduleResult result) {
        double totalWait = 0, totalTurn = 0;
        for (Process p : processes) {
            totalWait += p.getWaitingTime();
            totalTurn += p.getTurnaroundTime();
        }
        result.avgWaitingTime = totalWait / processes.size();
        result.avgTurnaroundTime = totalTurn / processes.size();
    }
}
