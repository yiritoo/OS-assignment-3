package model;
import java.util.ArrayList;
import java.util.List;

public class Process {
    // Static Input Parameters
    private String name;
    private int arrivalTime;
    private int totalBurstTime;
    private int initialPriority;
    private int initialQuantum;
    private int lastUpdate;  // آخر مرة تم تحديث الأولوية أو تم تنفيذ العملية
    private int pid;         // معرف العملية (لو مش موجود)


    // Dynamic Scheduling Parameters
    private int remainingBurstTime;
    private int currentQuantum;
    private int dynamicPriority;

    // Output Metrics (Set at completion)
    private int waitingTime;
    private int turnaroundTime;
    private int completionTime;
    private int startTime;
    private boolean started;// Flag to track if the process has started
    private ArrayList<Integer> quantumHistory;
    private int timeExecutedInCurrentQuantum;
    private int currentPhase;

    // Constructor
    public Process(String name, int arrivalTime, int totalBurstTime, int initialPriority, int initialQuantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.totalBurstTime = totalBurstTime;
        this.initialPriority = initialPriority;
        this.initialQuantum = initialQuantum;

        // Initialize dynamic variables
        this.remainingBurstTime = totalBurstTime;
        this.currentQuantum = initialQuantum;
        this.dynamicPriority = initialPriority;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.startTime = -1;
        this.started = false;
        this.quantumHistory = new ArrayList<>();
        this.quantumHistory.add(initialQuantum);
        this.timeExecutedInCurrentQuantum = 0;
        this.currentPhase = 1;
    }

    // Utility Methods
    public boolean isCompleted() {
        return remainingBurstTime <= 0;
    }

    public void logQuantumUpdate() {
        this.quantumHistory.add(this.currentQuantum);
    }

    // This method is critical for AG Scheduling and for applying context switch time
    public void execute(int timeSlice) {

        // Mark the process as started
        if (!started) {
            this.started = true;
        }

        // Decrement the total time required for the job
        this.remainingBurstTime -= timeSlice;

        // Increment the time used in the current quantum block
        this.timeExecutedInCurrentQuantum += timeSlice;
    }

    // Getters
    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getTotalBurstTime() { return totalBurstTime; }
    public int getInitialPriority() { return initialPriority; }
    public int getInitialQuantum() { return initialQuantum; }

    public int getRemainingBurstTime() { return remainingBurstTime; }
    public int getCurrentQuantum() { return currentQuantum; }
    public int getDynamicPriority() { return dynamicPriority; }

    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getCompletionTime() { return completionTime; }
    public int getStartTime() { return startTime; }
    public boolean hasStarted() { return started; }
    public ArrayList<Integer> getQuantumHistory() { return quantumHistory; }
    public int getTimeExecutedInCurrentQuantum() {return timeExecutedInCurrentQuantum;}
    public int getCurrentPhase() {return currentPhase;}

    // Setters
    public void setRemainingBurstTime(int remainingBurstTime) { this.remainingBurstTime = remainingBurstTime; }
    public void setCurrentQuantum(int currentQuantum) { this.currentQuantum = currentQuantum; }
    public void setDynamicPriority(int dynamicPriority) { this.dynamicPriority = dynamicPriority; }

    public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void setCompletionTime(int completionTime) { this.completionTime = completionTime; }
    public void setStartTime(int startTime) {
        if (!started) {
            this.startTime = startTime;
            this.started = true;
        }
    }
    public static ArrayList<Process> copyList(List<Process> input) {
        ArrayList<Process> copy = new ArrayList<>();
        for (Process p : input) {
            Process newP = new Process(p.name, p.arrivalTime, p.totalBurstTime, p.initialPriority, p.initialQuantum);
            newP.setDynamicPriority(p.getDynamicPriority());
            copy.add(newP);
        }
        return copy;
    }
    public int getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(int time) {
        this.lastUpdate = time;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}

