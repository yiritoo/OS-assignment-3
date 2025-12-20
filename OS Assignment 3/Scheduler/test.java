package Scheduler;
import com.google.gson.Gson;
import model.Process;
import Scheduler.*;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class test {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("Unit Test");
        System.out.println("============================");

        runTestSuite("AG", "AG");
        runTestSuite("Other_Schedulers", "OTHER");
    }

    private static void runTestSuite(String folderPath, String type) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (listOfFiles == null) return;

        for (File file : listOfFiles) {
            try (FileReader reader = new FileReader(file)) {
                Map<String, Object> data = gson.fromJson(reader, Map.class);
                Map<String, Object> input = (Map<String, Object>) data.get("input");
                Map<String, Object> expected = (Map<String, Object>) data.get("expectedOutput");
                List<Map<String, Object>> procData = (List<Map<String, Object>>) input.get("processes");

                List<Process> processes = new ArrayList<>();
                for (Map<String, Object> p : procData) {
                    processes.add(new Process(
                            (String) p.get("name"),
                            ((Double) p.get("arrival")).intValue(),
                            ((Double) p.get("burst")).intValue(),
                            ((Double) p.get("priority")).intValue(),
                            p.containsKey("quantum") ? ((Double) p.get("quantum")).intValue() : 0
                    ));
                }

                int cs = input.containsKey("contextSwitch") ? ((Double) input.get("contextSwitch")).intValue() : 0;
                int rrQ = input.containsKey("rrQuantum") ? ((Double) input.get("rrQuantum")).intValue() : 0;

                System.out.println("\nTest File: " + file.getName());

                if (type.equals("AG")) {
                    AG_Scheduler ag = new AG_Scheduler(new ArrayList<>(processes));
                    printCompare("AG", ag.schedule(processes, cs), expected);
                } else {
                    // Standard Schedulers Comparison
                    runStandardCompare(processes, cs, rrQ, expected);
                }
            } catch (Exception e) {
                System.err.println("Error processing " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private static void runStandardCompare(List<Process> data, int cs, int rrQ, Map<String, Object> expectedRoot) {
        if (expectedRoot.containsKey("SJF")) runAndCompare("SJF", new SJFScheduler(), data, cs, (Map<String, Object>) expectedRoot.get("SJF"));
        if (expectedRoot.containsKey("RR")) runAndCompare("Round Robin", new RoundRobinScheduler(rrQ), data, cs, (Map<String, Object>) expectedRoot.get("RR"));
        if (expectedRoot.containsKey("Priority")) runAndCompare("Priority", new PriorityScheduler(), data, cs, (Map<String, Object>) expectedRoot.get("Priority"));
    }

    private static void runAndCompare(String name, Scheduler s, List<Process> data, int cs, Map<String, Object> expected) {
        printCompare(name, s.schedule(Process.copyList(data), cs), expected);
    }

    private static void printCompare(String name, ScheduleResult actual, Map<String, Object> expected) {
        System.out.println("\nScheduler: " + name);

        // Actual Results (Clean Format)
        System.out.println("Result");
        System.out.println("Order: " + String.join(" -> ", actual.executionOrder));
        System.out.printf("%-10s  %-12s  %-15s  %-15s\n", "Process", "Burst", "Wait Time", "Turnaround");
        for (Process p : actual.processes) {
            System.out.printf("%-10s  %-12d  %-15d  %-15d\n", p.getName(), p.getTotalBurstTime(), p.getWaitingTime(), p.getTurnaroundTime());
        }
        System.out.printf("Avg Wait: %.2f  Avg TAT: %.2f\n", actual.avgWaitingTime, actual.avgTurnaroundTime);

        // Expected Results from JSON
        System.out.println("\nExpected Result");
        System.out.println("Order: " + expected.get("executionOrder"));
        System.out.printf("Avg Wait: %s  Avg TAT: %s\n", expected.get("averageWaitingTime"), expected.get("averageTurnaroundTime"));
        System.out.println("......................................................................");
    }
}