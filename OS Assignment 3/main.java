import model.Process;
import Scheduler.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // 1. Read System Parameters
        System.out.print("Enter number of processes: ");
        int n = in.nextInt();

        System.out.print("Enter Round Robin Quantum: ");
        int rrQuantum = in.nextInt();

        System.out.print("Enter context switching time: ");
        int contextSwitchTime = in.nextInt();

        List<Process> processes = new ArrayList<>();

        // 2. Read each process data
        // Order: name, arrivalTime, burstTime, priority, (AG) quantum
        for (int i = 0; i < n; i++) {
            System.out.println("\nEnter details for Process " + (i + 1) + ":");
            System.out.print("Name: ");
            String name = in.next();
            System.out.print("Arrival Time: ");
            int arrivalTime = in.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = in.nextInt();
            System.out.print("Priority: ");
            int priority = in.nextInt();
            System.out.print("AG Quantum: ");
            int agQuantum = in.nextInt();

            processes.add(new Process(name, arrivalTime, burstTime, priority, agQuantum));
        }

        // 3. Execute and Print Results for each Scheduler
        // We use Process.copyList to ensure each scheduler starts with original data

        // ---- SJF Scheduler ----
        runAndPrintResults("Shortest Job First (Preemptive)", new SJFScheduler(), processes, contextSwitchTime);

        // ---- Round Robin Scheduler ----
        runAndPrintResults("Round Robin", new RoundRobinScheduler(rrQuantum), processes, contextSwitchTime);

        // ---- Priority Scheduler (with Aging) ----
        runAndPrintResults("Preemptive Priority (with Aging)", new PriorityScheduler(), processes, contextSwitchTime);

        // ---- AG Scheduler ----
        // AG Scheduler requires printing the specific quantum history lines
        System.out.println("\n========================================");
        System.out.println("Running: AG Scheduler");
        AG_Scheduler ag = new AG_Scheduler(Process.copyList(processes));
        ScheduleResult agReport = ag.schedule(processes, contextSwitchTime);
        
        System.out.println("Execution Order: " + String.join(" -> ", agReport.executionOrder));
        System.out.println("\nQuantum History Updates:");
        for (String line : agReport.quantumHistoryLines) {
            System.out.println(line);
        }
        System.out.println("\nAverage Waiting Time: " + agReport.avgWaitingTime);
        System.out.println("Average Turnaround Time: " + agReport.avgTurnaroundTime);
        System.out.println("========================================\n");

        in.close();
    }

    /**
     * Helper method to run a scheduler and print its standard results.
     */
    private static void runAndPrintResults(String schedulerName, Scheduler scheduler, List<Process> originalList, int contextSwitch) {
        // Always work on a deep copy
        List<Process> inputCopy = Process.copyList(originalList);
        ScheduleResult result = scheduler.schedule(inputCopy, contextSwitch);

        System.out.println("\n========================================");
        System.out.println("Running: " + schedulerName);
        System.out.println("Execution Order: " + String.join(" -> ", result.executionOrder));
        
        System.out.println("\nProcess Metrics:");
        for (Process p : result.processes) {
            System.out.println("Process: " + p.getName() + 
                               " | Waiting Time: " + p.getWaitingTime() + 
                               " | Turnaround Time: " + p.getTurnaroundTime());
        }

        System.out.println("\nAverage Waiting Time: " + result.avgWaitingTime);
        System.out.println("Average Turnaround Time: " + result.avgTurnaroundTime);
        System.out.println("========================================\n");
    }
}
