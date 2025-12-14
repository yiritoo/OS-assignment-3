package Scheduler;
import model.Process;
import java.util.List;

public class PriorityScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();
        report.processes = processes;

        // TODO: Preemptive Priority Scheduling
        // [ ] choose highest priority among ready processes
        // [ ] preempt when higher-priority arrives
        // [ ] apply context switch time on preemption/switch
        // [ ] starvation solution REQUIRED (aging)
        //     Example TODO:
        //     [ ] every X time units, improve priority of waiting processes

        return report;
    }
}
