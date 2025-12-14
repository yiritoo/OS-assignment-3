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

        // Initialize remaining time
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.waitingTime = 0;
            p.turnaroundTime = 0;
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

            // Context switch
            if (current != null && current != next) {
                // Pay the context switch overhead
                if (contextSwitchTime > 0) {
                    time += contextSwitchTime;
                }
            }

            // After context switch time passes, pick the shortest process again
            next = pickShortestRemaining(processes, time);
            if (next == null) {
                time += 1;
                current = null;
                continue;
            }
            current = next;

            // Execute for 1 time unit (preemptive granularity)
            current.remainingTime -= 1;
            time += 1;

            // If process finished, record stats
            if (current.remainingTime == 0) {
                finished++;

                int completionTime = time;
                current.turnaroundTime = completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                current = null;
            }
        }
        getAverages(result);

        return result;
    }

    private Process pickShortestRemaining(List<Process> processes, int time) {
        Process shortest = null;

        for (Process p : processes) {
            if (p.arrivalTime <= time && p.remainingTime > 0) {
                if (shortest == null) {
                    shortest = p;
                } else if (p.remainingTime < shortest.remainingTime) {
                    shortest = p;
                } else if (p.remainingTime == shortest.remainingTime) {
                    if (p.arrivalTime < shortest.arrivalTime) {
                        shortest = p;
                    } else if (p.arrivalTime == shortest.arrivalTime) {
                        if (p.name.compareTo(shortest.name) < 0)
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

        for (Process p : result.processes) {
            sumwaiting += p.waitingTime;
            sumturn += p.turnaroundTime;
        }

        int n = result.processes.size();
        result.avgWaitingTime = (n == 0) ? 0 : (sumwaiting / n);
        result.avgTurnaroundTime = (n == 0) ? 0 : (sumturn / n);
    }
}
