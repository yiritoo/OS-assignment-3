package Scheduler;

import model.Process;
import java.util.List;

public class AGScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();
        report.processes = processes;

        // TODO: AG Scheduling (per assignment rules) :contentReference[oaicite:2]{index=2}
        // [ ] Maintain ready queue
        // [ ] For running process with quantum Q:
        //     [ ] Run FCFS for ceil(0.25 * Q)
        //     [ ] Then non-preemptive Priority for next ceil(0.25 * Q)
        //     [ ] Then preemptive SJF for remaining slice
        //
        // [ ] Handle quantum update scenarios:
        //     (i) used all Q and not finished -> enqueue; Q += 2
        //     (ii) preempted during Priority -> enqueue; Q += ceil(remainingQ/2)
        //     (iii) preempted during SJF -> enqueue; Q += remainingQ
        //     (iv) finished -> Q = 0
        //
        // [ ] Save & print quantum history for each process
        //     report.quantumHistoryLines.add("P1: 7 -> 10 -> 0");
        return report;
    }
}
