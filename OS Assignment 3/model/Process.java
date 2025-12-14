package model;
import java.util.ArrayList;
import java.util.List;

public class Process {
    public String name;
    public int arrivalTime;
    public int burstTime;
    public int remainingTime;
    public int priority;
    public int quantum;

    public int waitingTime;
    public int turnaroundTime;
    public int completionTime;

    public Process() {}

    public static List<Process> copyList(List<Process> input) {
        List<Process> copy = new ArrayList<>();
        for (Process p : input) {
            Process c = new Process();
            c.name = p.name;
            c.arrivalTime = p.arrivalTime;
            c.burstTime = p.burstTime;
            c.remainingTime = p.burstTime;
            c.priority = p.priority;
            c.quantum = p.quantum;

            c.waitingTime = 0;
            c.turnaroundTime = 0;
            c.completionTime = 0;
            copy.add(c);
        }
        return copy;
    }
}
