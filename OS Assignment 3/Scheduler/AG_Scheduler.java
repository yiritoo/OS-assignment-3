package Scheduler;

import model.Process;
import java.util.*;

public class AG_Scheduler implements Scheduler {

    private Map<Process, Integer> remainingQuantum = new HashMap<>();

    @Override
    public ScheduleResult schedule(List<Process> input, int contextSwitch) {

        List<Process> processes = Process.copyList(input);
        Queue<Process> readyQueue = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();

        int time = 0;
        int completed = 0;
        Process current = null;
        boolean forceSwitch = false;

        // init
        for (Process p : processes) {
            remainingQuantum.put(p, p.getInitialQuantum());
        }

        addArrived(readyQueue, processes, time, null);

        while (completed < processes.size()) {

            if (!forceSwitch) {
                current = readyQueue.poll();
            }
            forceSwitch = false;

            if (current == null) {
                time++;
                addArrived(readyQueue, processes, time, null);
                continue;
            }

            executionOrder.add(current.getName());

            int rq = remainingQuantum.get(current);

            // ================== FCFS (25%) ==================
            int slice = (int) Math.ceil(0.25 * current.getCurrentQuantum());
            int exe = Math.min(slice, current.getRemainingBurstTime());

            current.execute(exe);
            time += exe;
            rq -= exe;

            remainingQuantum.put(current, rq);
            addArrived(readyQueue, processes, time, current);

            if (current.isCompleted()) {
                finish(current, time);
                completed++;
                continue;
            }

            // ================== PRIORITY ==================
            Process highestPriority = getMostPriority(processes, time);

            if (highestPriority != null && highestPriority != current) {

                int bonus = (int) Math.ceil(rq / 2.0);
                current.setCurrentQuantum(current.getCurrentQuantum() + bonus);
                current.logQuantumUpdate();

                remainingQuantum.put(current, current.getCurrentQuantum());
                readyQueue.add(current);

                current = highestPriority;
                readyQueue.remove(highestPriority);
                forceSwitch = true;
                continue;
            }

            // execute another 25%
            exe = Math.min(slice, current.getRemainingBurstTime());
            current.execute(exe);
            time += exe;
            rq -= exe;

            remainingQuantum.put(current, rq);
            addArrived(readyQueue, processes, time, current);

            if (current.isCompleted()) {
                finish(current, time);
                completed++;
                continue;
            }

            // ================== SJF ==================
            Process shortest = getShortest(processes, time);

            if (shortest != current &&
                    shortest.getRemainingBurstTime() < current.getRemainingBurstTime()) {

                current.setCurrentQuantum(current.getCurrentQuantum() + rq);
                current.logQuantumUpdate();

                remainingQuantum.put(current, current.getCurrentQuantum());
                readyQueue.add(current);

                current = shortest;
                readyQueue.remove(shortest);
                forceSwitch = true;
                continue;
            }

            // run remaining quantum
            exe = Math.min(rq, current.getRemainingBurstTime());
            current.execute(exe);
            time += exe;
            rq -= exe;

            remainingQuantum.put(current, rq);
            addArrived(readyQueue, processes, time, current);

            if (current.isCompleted()) {
                finish(current, time);
                completed++;
                continue;
            }

            // ================== QUANTUM EXHAUSTED ==================
            current.setCurrentQuantum(current.getCurrentQuantum() + 2);
            current.logQuantumUpdate();
            remainingQuantum.put(current, current.getCurrentQuantum());
            readyQueue.add(current);
        }

        // ================== RESULT ==================
        ScheduleResult result = new ScheduleResult();
        result.executionOrder = executionOrder;
        result.processes = processes;

        double wt = 0, tat = 0;
        for (Process p : processes) {
            wt += p.getWaitingTime();
            tat += p.getTurnaroundTime();
        }

        result.avgWaitingTime = wt / processes.size();
        result.avgTurnaroundTime = tat / processes.size();

        return result;
    }

    // ================== HELPERS ==================

    private void finish(Process p, int time) {
        p.setCompletionTime(time);
        p.setTurnaroundTime(time - p.getArrivalTime());
        p.setWaitingTime(p.getTurnaroundTime() - p.getTotalBurstTime());
        p.setCurrentQuantum(0);
        p.logQuantumUpdate();
    }

    private void addArrived(
            Queue<Process> q,
            List<Process> all,
            int time,
            Process running
    ) {
        for (Process p : all) {
            if (p.getArrivalTime() <= time &&
                    !p.isCompleted() &&
                    p != running &&
                    !q.contains(p)) {
                q.add(p);
            }
        }
    }

    private Process getMostPriority(List<Process> all, int time) {
        Process best = null;
        for (Process p : all) {
            if (p.getArrivalTime() <= time && !p.isCompleted()) {
                if (best == null ||
                        p.getDynamicPriority() < best.getDynamicPriority()) {
                    best = p;
                }
            }
        }
        return best;
    }

    private Process getShortest(List<Process> all, int time) {
        Process best = null;
        for (Process p : all) {
            if (p.getArrivalTime() <= time && !p.isCompleted()) {
                if (best == null ||
                        p.getRemainingBurstTime() < best.getRemainingBurstTime()) {
                    best = p;
                }
            }
        }
        return best;
    }
}
