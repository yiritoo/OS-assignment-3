package Scheduler;
import model.Process;
import java.util.ArrayList;
import java.util.List;

public class ScheduleResult{
    public List<String> executionOrder = new ArrayList<>();
    public List<Process> processes = new ArrayList<>();

    public double avgWaitingTime;
    public double avgTurnaroundTime;

    // AG only
    public List<String> quantumHistoryLines = new ArrayList<>();
}
