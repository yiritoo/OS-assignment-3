package Scheduler;
import model.Process;
import java.util.*;

public class AG_Scheduler implements Scheduler {

    private final List<Process> allProcesses;
    private final PriorityQueue<Process> readyQueue;

    public AG_Scheduler(List<Process> processes) {
        this.allProcesses = processes;
        this.readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(Process::getDynamicPriority)
        );
    }

    @Override
    public ScheduleResult schedule(List<Process> processes, int contextSwitchTime) {
        return runAGScheduling(contextSwitchTime);
    }

    private int calculatePhaseSlice(int currentQuantum) {
        return (int) Math.ceil(currentQuantum * 0.25);
    }

    private int calculateScenarioTwoIncrease(int remainingQuantum) {
        return (int) Math.ceil(remainingQuantum / 2.0);
    }

    public void updateQuantum(Process process, int timeUsedInQuantum, int preemptionScenario) {
        int oldQuantum = process.getCurrentQuantum();
        int remainingQuantum = oldQuantum - timeUsedInQuantum;
        int newQuantum = 0;

        switch (preemptionScenario) {
            case 1:
                newQuantum = oldQuantum + 2;
                break;
            case 2:
                newQuantum = oldQuantum + calculateScenarioTwoIncrease(remainingQuantum);
                break;
            case 3:
                newQuantum = oldQuantum + remainingQuantum;
                break;
            case 4:
                newQuantum = 0;
                break;
        }

        process.setCurrentQuantum(newQuantum);
        process.logQuantumUpdate();
    }

    public ScheduleResult runAGScheduling(int contextSwitch) {
        ScheduleResult report = new ScheduleResult();
        report.processes = allProcesses;

        int currentTime = 0;
        int completedCount = 0;
        Process runningProcess = null;
        List<Process> arrivalList = new ArrayList<>(allProcesses);
        arrivalList.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (completedCount < allProcesses.size()) {
            Iterator<Process> it = arrivalList.iterator();
            while (it.hasNext()) {
                Process p = it.next();
                if (p.getArrivalTime() <= currentTime) {
                    readyQueue.add(p);
                    it.remove();
                }
            }

            if (runningProcess == null && !readyQueue.isEmpty()) {
                runningProcess = readyQueue.poll();
                currentTime += contextSwitch;
            }

            if (runningProcess != null) {
                report.executionOrder.add(runningProcess.getName());

                int q = runningProcess.getCurrentQuantum();
                int q1_limit = calculatePhaseSlice(q);
                int q2_limit = q1_limit + calculatePhaseSlice(q);

                while (runningProcess.getTimeExecutedInCurrentQuantum() < q) {
                    runningProcess.execute(1);
                    currentTime++;

                    it = arrivalList.iterator();
                    while (it.hasNext()) {
                        Process p = it.next();
                        if (p.getArrivalTime() <= currentTime) {
                            readyQueue.add(p);
                            it.remove();
                        }
                    }

                    if (runningProcess.isCompleted()) {
                        updateQuantum(runningProcess, runningProcess.getTimeExecutedInCurrentQuantum(), 4);
                        completedCount++;
                        runningProcess.setCompletionTime(currentTime);
                        runningProcess = null;
                        break;
                    }

                    int used = runningProcess.getTimeExecutedInCurrentQuantum();

                    if (used == q1_limit || used == q2_limit) {
                        Process bestInQueue = readyQueue.peek();
                        if (bestInQueue != null && bestInQueue.getDynamicPriority() < runningProcess.getDynamicPriority()) {
                            updateQuantum(runningProcess, used, 2);
                            readyQueue.add(runningProcess);
                            runningProcess = null;
                            break;
                        }
                    } else if (used > q2_limit) {
                        Process shortestInQueue = null;
                        for (Process p : readyQueue) {
                            if (shortestInQueue == null || p.getRemainingBurstTime() < shortestInQueue.getRemainingBurstTime()) {
                                shortestInQueue = p;
                            }
                        }
                        if (shortestInQueue != null && shortestInQueue.getRemainingBurstTime() < runningProcess.getRemainingBurstTime()) {
                            updateQuantum(runningProcess, used, 3);
                            readyQueue.add(runningProcess);
                            runningProcess = null;
                            break;
                        }
                    }

                    if (runningProcess != null && runningProcess.getTimeExecutedInCurrentQuantum() == q) {
                        updateQuantum(runningProcess, q, 1);
                        readyQueue.add(runningProcess);
                        runningProcess = null;
                        break;
                    }
                }
            } else {
                currentTime++;
            }
        }

        calculateMetrics(report);
        return report;
    }

    private void calculateMetrics(ScheduleResult result) {
        double totalWT = 0;
        double totalTAT = 0;

        for (Process p : result.processes) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(tat);
            int wt = tat - p.getTotalBurstTime();
            p.setWaitingTime(wt);

            totalTAT += tat;
            totalWT += wt;
            result.quantumHistoryLines.add(p.getName() + " History: " + p.getQuantumHistory().toString());
        }

        if (!result.processes.isEmpty()) {
            result.avgTurnaroundTime = totalTAT / result.processes.size();
            result.avgWaitingTime = totalWT / result.processes.size();
        }
    }
}