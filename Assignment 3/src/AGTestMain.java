import schedulers.AGScheduler;
import schedulers.Scheduler;
import models.Process;
import models.SchedulerResult;
import java.util.ArrayList;
import java.util.List;

public class AGTestMain {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 17, 4, 7));
        processes.add(new Process("P2", 2, 6, 7, 9));
        processes.add(new Process("P3", 5, 11, 3, 4));
        processes.add(new Process("P4", 15, 4, 6, 6));
        Scheduler scheduler = new AGScheduler();
        SchedulerResult result = scheduler.schedule(processes, 0, 4);
        printResult(result);
    }

    private static void printResult(SchedulerResult result) {
        System.out.println("\n==============================");
        System.out.println(" " + result.schedulerName);
        System.out.println("==============================");
        System.out.println("\nExecution Order:");
        for (String p : result.executionOrder)
            System.out.print(p + " | ");
        System.out.println("\n\nProcess Details:");
        System.out.printf("%-8s %-10s %-10s %-10s %-12s %-12s\n", "Process", "Arrival", "Burst", "Priority", "Waiting",
                "Turnaround");
        for (SchedulerResult.ProcessResult pr : result.processResults) {
            System.out.printf("%-8s %-10d %-10d %-10d %-12d %-12d\n", pr.name, pr.arrivalTime, pr.burstTime,
                    pr.priority, pr.waitingTime, pr.turnaroundTime);
            System.out.println(" " + pr.extraInfo);
        }
        System.out.println("\nAverage Waiting Time = " + result.avgWaitingTime);
        System.out.println("Average Turnaround Time = " + result.avgTurnaroundTime);
    }
}