package Scheduler;

import model.Process;
import java.util.List;

public interface Scheduler {
    ScheduleResult schedule(List<Process> processes, int contextSwitchTime);
}