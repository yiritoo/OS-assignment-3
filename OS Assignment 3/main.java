import model.Process;
import Scheduler.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // 1. UPDATED INPUT HANDLING: Added a clear header for Abdelrahman's task
        System.out.println("========================================");
        System.out.println("   OS SCHEDULER INPUT CONFIGURATION   ");
        System.out.println("========================================");

        System.out.print("Enter number of processes: ");
        int n = in.nextInt();

        System.out.print("Enter Round Robin Quantum: ");
        int rrQuantum = in.nextInt();

        System.out.print("Enter context switching time: ");
        int contextSwitchTime = in.nextInt();

        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Process " + (i + 1) + " Configuration ---");
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

        // 2. EXECUTION: Standard Schedulers
        runAndPrintResults("Shortest Job First (Preemptive)", new SJFScheduler(), processes, contextSwitchTime);
        runAndPrintResults("Round Robin", new RoundRobinScheduler(rrQuantum), processes, contextSwitchTime);
        runAndPrintResults("Preemptive Priority (with Aging)", new PriorityScheduler(), processes, contextSwitchTime);

        // 3. UPDATED AG OUTPUT: Specialized formatting for AG History
        System.out.println("\n========================================");
        System.out.println("RUNNING: AG SCHEDULER");
        System.out.println("========================================");

        // Use copy to preserve original process data
        AG_Scheduler ag = new AG_Scheduler();
        ScheduleResult agReport = ag.schedule(processes, contextSwitchTime);

        // Task: Print Execution Order
        System.out.println("Execution Order: " + String.join(" -> ", agReport.executionOrder));

        // Task: Print Metric Table
        printFormattedTable(agReport.processes);

        // Task: Print Quantum History
        System.out.println("\nQuantum History Updates:");
        for (String line : agReport.quantumHistoryLines) {
            System.out.println("  > " + line);
        }

        System.out.printf("\nAverage Waiting Time: %.2f\n", agReport.avgWaitingTime);
        System.out.printf("Average Turnaround Time: %.2f\n", agReport.avgTurnaroundTime);
        System.out.println("========================================\n");

        in.close();
    }

    /**
     * UPDATED: Helper method to run a scheduler and print professional Metric Tables
     */
    private static void runAndPrintResults(String schedulerName, Scheduler scheduler, List<Process> originalList, int contextSwitch) {
        List<Process> inputCopy = Process.copyList(originalList);
        ScheduleResult result = scheduler.schedule(inputCopy, contextSwitch);

        System.out.println("\n========================================");
        System.out.println("RUNNING: " + schedulerName);
        System.out.println("========================================");
        System.out.println("Execution Order: " + String.join(" -> ", result.executionOrder));

        // Call the table formatter for consistent metric output
        printFormattedTable(result.processes);

        System.out.printf("\nAverage Waiting Time: %.2f\n", result.avgWaitingTime);
        System.out.printf("Average Turnaround Time: %.2f\n", result.avgTurnaroundTime);
        System.out.println("========================================\n");
    }

    /**
     * NEW: Helper method to print data in a table format as required by Abdelrahman's task
     */
    private static void printFormattedTable(List<Process> processes) {
        System.out.println("\nMETRIC TABLE:");
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-12s | %-12s\n", "Process", "Burst", "Waiting", "Turnaround");
        System.out.println("---------------------------------------------------------");
        for (Process p : processes) {
            System.out.printf("%-10s | %-12d | %-12d | %-12d\n",
                    p.getName(), p.getTotalBurstTime(), p.getWaitingTime(), p.getTurnaroundTime());
        }
        System.out.println("---------------------------------------------------------");
    }
}