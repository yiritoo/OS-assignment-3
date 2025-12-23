package Scheduler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Process;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class test {

    private static final boolean OUTPUT_TO_JSON = false;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Map<String, Object>> allResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Running Tests...");
        System.out.println("Output Mode: " + (OUTPUT_TO_JSON ? "JSON File" : "Terminal"));
        System.out.println("============================");

        runTestSuite("AG", "AG");
        runTestSuite("Other_Schedulers", "OTHER");

        // If JSON mode is on, write the accumulated file at the end
        if (OUTPUT_TO_JSON) {
            try (FileWriter writer = new FileWriter("output.json")) {
                gson.toJson(allResults, writer);
                System.out.println("\nSuccess! Results written to 'output.json'");
            } catch (IOException e) {
                System.err.println("Error writing output file: " + e.getMessage());
            }
        }
    }

    private static void runTestSuite(String folderPath, String type) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            System.out.println("Directory not found: " + folderPath);
            return;
        }

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
                int agingInterval = input.containsKey("agingInterval") ? ((Double) input.get("agingInterval")).intValue() : 5;

                if (!OUTPUT_TO_JSON) {
                    System.out.println("\nProcessing Test File: " + file.getName());
                }

                if (type.equals("AG")) {
                    AG_Scheduler ag = new AG_Scheduler();
                    handleResult(file.getName(), "AG", ag.schedule(processes, cs), expected);
                } else {
                    runStandardCompare(file.getName(), processes, cs, rrQ, agingInterval, expected);
                }
            } catch (Exception e) {
                System.err.println("Error processing " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private static void runStandardCompare(String fileName, List<Process> data, int cs, int rrQ, int agingInterval, Map<String, Object> expectedRoot) {
        if (expectedRoot.containsKey("SJF"))
            runAndCompare(fileName, "SJF", new SJFScheduler(), data, cs, agingInterval, (Map<String, Object>) expectedRoot.get("SJF"));

        if (expectedRoot.containsKey("RR"))
            runAndCompare(fileName, "Round Robin", new RoundRobinScheduler(rrQ), data, cs, agingInterval, (Map<String, Object>) expectedRoot.get("RR"));

        if (expectedRoot.containsKey("Priority"))
            runAndComparePriority(fileName, "Priority", new PriorityScheduler(), data, cs, agingInterval, (Map<String, Object>) expectedRoot.get("Priority"));
    }

    private static void runAndCompare(String fileName, String schedulerName, Scheduler s, List<Process> data, int cs, int agingInterval, Map<String, Object> expected) {
        handleResult(fileName, schedulerName, s.schedule(Process.copyList(data), cs), expected);
    }

    private static void runAndComparePriority(String fileName, String schedulerName, PriorityScheduler s, List<Process> data, int cs, int agingInterval, Map<String, Object> expected) {
        // FIX: Pass agingInterval to the schedule method
        handleResult(fileName, schedulerName, s.schedule(Process.copyList(data), cs, agingInterval), expected);
    }

    private static void handleResult(String fileName, String schedulerName, ScheduleResult actual, Map<String, Object> expected) {
        if (OUTPUT_TO_JSON) {
            addToJsonList(fileName, schedulerName, actual, expected);
        } else {
            printToTerminal(schedulerName, actual, expected);
        }
    }

    private static void printToTerminal(String name, ScheduleResult actual, Map<String, Object> expected) {
        System.out.println("Scheduler: " + name);
        System.out.println("Result");
        System.out.println("Order: " + String.join(" -> ", actual.executionOrder));
        System.out.printf("%-10s  %-12s  %-15s  %-15s\n", "Process", "Burst", "Wait Time", "Turnaround");
        for (Process p : actual.processes) {
            System.out.printf("%-10s  %-12d  %-15d  %-15d\n", p.getName(), p.getTotalBurstTime(), p.getWaitingTime(), p.getTurnaroundTime());
        }
        System.out.printf("Avg Wait: %.2f  Avg TAT: %.2f\n", actual.avgWaitingTime, actual.avgTurnaroundTime);

        System.out.println("\nExpected Result");
        System.out.println("Order: " + expected.get("executionOrder"));
        System.out.printf("Avg Wait: %s  Avg TAT: %s\n", expected.get("averageWaitingTime"), expected.get("averageTurnaroundTime"));
        System.out.println("......................................................................");
    }

    // Logic for JSON Collection
    private static void addToJsonList(String fileName, String schedulerName, ScheduleResult actual, Map<String, Object> expected) {
        Map<String, Object> resultEntry = new LinkedHashMap<>();
        resultEntry.put("testFile", fileName);
        resultEntry.put("scheduler", schedulerName);

        Map<String, Object> actualMap = new LinkedHashMap<>();
        actualMap.put("executionOrder", actual.executionOrder);
        actualMap.put("averageWaitingTime", actual.avgWaitingTime);
        actualMap.put("averageTurnaroundTime", actual.avgTurnaroundTime);

        List<Map<String, Object>> procDetails = new ArrayList<>();
        for (Process p : actual.processes) {
            Map<String, Object> pInfo = new LinkedHashMap<>();
            pInfo.put("name", p.getName());
            pInfo.put("waitingTime", p.getWaitingTime());
            pInfo.put("turnaroundTime", p.getTurnaroundTime());
            procDetails.add(pInfo);
        }
        actualMap.put("processes", procDetails);

        resultEntry.put("actualOutput", actualMap);
        resultEntry.put("expectedOutput", expected);

        allResults.add(resultEntry);
    }
}