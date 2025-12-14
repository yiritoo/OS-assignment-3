package Scheduler;
import model.Process;
import java.util.List;

public class RoundRobinScheduler implements Scheduler {

    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public ScheduleResult schedule(List<Process> processes, int contextSwitchTime) {
        ScheduleResult report = new ScheduleResult();
        report.processes = processes;

        // TODO: Round Robin
        // [ ] ready queue (FCFS)
        // [ ] run process for min(quantum, remainingTime)
        // [ ] apply context switch when moving to another process
        // [ ] append to executionOrder only when CPU switches
        // [ ] set completionTime when finished

        return report;
    }
}