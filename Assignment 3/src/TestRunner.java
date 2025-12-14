import models.Process;
import models.SchedulerResult;
import schedulers.PriorityScheduler;
import utils.OutputFormatter;
import java.util.*;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║         PRIORITY SCHEDULER TEST RUNNER               ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        PriorityScheduler scheduler = new PriorityScheduler();

        runTest1(scheduler);
        runTest2(scheduler);
        runTest3(scheduler);
        runTest4(scheduler);
    }

    private static void runTest1(PriorityScheduler scheduler) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST 1: Basic mixed arrivals");
        System.out.println("=".repeat(70));

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 8, 3, 0),
                new Process("P2", 1, 4, 1, 0),
                new Process("P3", 2, 2, 4, 0),
                new Process("P4", 3, 1, 2, 0),
                new Process("P5", 4, 3, 5, 0)
        );

        SchedulerResult result = scheduler.schedule(processes, 1, 5);
        OutputFormatter.printResult(result);

        System.out.println("\nEXPECTED:");
        System.out.println("Execution Order: P1 → P2 → P1 → P4 → P1 → P3 → P1 → P5 → P1 → P3 → P5");
        System.out.println("P1: WT=15, TAT=23");
        System.out.println("P2: WT=1, TAT=5");
        System.out.println("P3: WT=21, TAT=23");
        System.out.println("P4: WT=6, TAT=7");
        System.out.println("P5: WT=21, TAT=24");
        System.out.println("Avg WT=12.8, Avg TAT=16.4");
    }

    private static void runTest2(PriorityScheduler scheduler) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST 2: All processes arrive at time 0");
        System.out.println("=".repeat(70));

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 6, 3, 0),
                new Process("P2", 0, 3, 1, 0),
                new Process("P3", 0, 8, 2, 0),
                new Process("P4", 0, 4, 4, 0),
                new Process("P5", 0, 2, 5, 0)
        );

        SchedulerResult result = scheduler.schedule(processes, 1, 5);
        OutputFormatter.printResult(result);

        System.out.println("\nEXPECTED:");
        System.out.println("Execution Order: P2 → P3 → P1 → P3 → P4 → P1 → P3 → P5 → P3 → P1 → P4 → P3 → P1 → P3 → P5 → P4 → P1 → P4");
        System.out.println("P1: WT=32, TAT=38");
        System.out.println("P2: WT=0, TAT=3");
        System.out.println("P3: WT=23, TAT=31");
        System.out.println("P4: WT=36, TAT=40");
        System.out.println("P5: WT=31, TAT=33");
        System.out.println("Avg WT=24.4, Avg TAT=29.0");
    }

    private static void runTest3(PriorityScheduler scheduler) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST 3: Processes with different arrival times");
        System.out.println("=".repeat(70));

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 10, 2, 0),
                new Process("P2", 2, 5, 1, 0),
                new Process("P3", 4, 3, 3, 0),
                new Process("P4", 6, 7, 4, 0)
        );

        SchedulerResult result = scheduler.schedule(processes, 1, 5);
        OutputFormatter.printResult(result);

        System.out.println("\nEXPECTED:");
        System.out.println("Execution Order: P1 → P2 → P1 → P3 → P1 → P4 → P1 → P3 → P1 → P4 → P1 → P3 → P4");
        System.out.println("P1: WT=18, TAT=28");
        System.out.println("P2: WT=1, TAT=6");
        System.out.println("P3: WT=18, TAT=21");
        System.out.println("P4: WT=20, TAT=27");
        System.out.println("Avg WT=14.25, Avg TAT=20.5");
    }

    private static void runTest4(PriorityScheduler scheduler) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST 4: Same priority processes");
        System.out.println("=".repeat(70));

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 4, 2, 0),
                new Process("P2", 1, 3, 2, 0),
                new Process("P3", 2, 2, 2, 0),
                new Process("P4", 3, 5, 2, 0)
        );

        SchedulerResult result = scheduler.schedule(processes, 1, 5);
        OutputFormatter.printResult(result);

        System.out.println("\nEXPECTED:");
        System.out.println("Execution Order: P1 → P2 → P1 → P3 → P1 → P4 → P1 → P2 → P4 → P2 → P3 → P4 → P4");
        System.out.println("P1: WT=7, TAT=11");
        System.out.println("P2: WT=13, TAT=16");
        System.out.println("P3: WT=11, TAT=13");
        System.out.println("P4: WT=11, TAT=16");
        System.out.println("Avg WT=10.5, Avg TAT=14.0");
    }
}
