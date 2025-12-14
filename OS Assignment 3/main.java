import model.Process;
import Scheduler.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        // TODO: paste TODO block here (optional)

        Scanner in = new Scanner(System.in);

        // TODO: Read number of processes
        int n = 0;

        // TODO: Read round robin quantum
        int rrQuantum = 0;

        // TODO: Read context switching time
        int contextSwitchTime = 0;

        List<Process> processes = new ArrayList<>();

        // TODO: Read each process:
        // name, arrivalTime, burstTime, priority, (AG) quantum
        for (int i = 0; i < n; i++) {
            Process p = new Process();
            // TODO: fill p fields from input
            processes.add(p);
        }

        // IMPORTANT: Each scheduler must start from the original input
        // TODO: either deep-copy processes before each scheduler or build fresh lists.

        Scheduler sjf = new SJFScheduler();
        Scheduler rr = new RoundRobinScheduler(rrQuantum);
        Scheduler pr = new PriorityScheduler();      // TODO: include aging inside
        Scheduler ag = new AGScheduler();            // TODO: print quantum history

        // ---- Run SJF ----
        List<Process> sjfInput = Process.copyList(processes);
        ScheduleResult sjfReport = sjf.schedule(sjfInput, contextSwitchTime);

        // ---- Run RR ----
        List<Process> rrInput = Process.copyList(processes);
        ScheduleResult rrReport = rr.schedule(rrInput, contextSwitchTime);

        // ---- Run Priority ----
        List<Process> prInput = Process.copyList(processes);
        ScheduleResult prReport = pr.schedule(prInput, contextSwitchTime);

        // ---- Run AG ----
        List<Process> agInput = Process.copyList(processes);
        ScheduleResult agReport = ag.schedule(agInput, contextSwitchTime);

        in.close();
    }
}
