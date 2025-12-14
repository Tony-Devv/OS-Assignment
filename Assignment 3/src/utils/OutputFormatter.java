package utils;

import models.SchedulerResult;
import models.SchedulerResult.ProcessResult;

public class OutputFormatter {

    public static void printResult(SchedulerResult result) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println(result.schedulerName);
        System.out.println("=".repeat(70));
        
        printExecutionOrder(result);
        printProcessDetails(result);
        printAverages(result);
    }
    
    private static void printExecutionOrder(SchedulerResult result) {
        System.out.println("\nExecution Order:");
        System.out.println("-".repeat(70));
        
        if (!result.executionOrder.isEmpty()) {
            System.out.println(String.join(" → ", result.executionOrder));
        }
    }
    
    private static void printProcessDetails(SchedulerResult result) {
        System.out.println("\nProcess Details:");
        System.out.println("-".repeat(70));
        System.out.printf("%-10s %-10s %-10s %-10s %-15s %-15s%n", 
                         "Process", "Arrival", "Burst", "Priority", "Waiting Time", "Turnaround Time");
        System.out.println("-".repeat(70));
        
        for (ProcessResult pr : result.processResults) {
            System.out.printf("%-10s %-10d %-10d %-10d %-15d %-15d%n",
                            pr.name,
                            pr.arrivalTime,
                            pr.burstTime,
                            pr.priority,
                            pr.waitingTime,
                            pr.turnaroundTime);
            
            if (!pr.extraInfo.isEmpty()) {
                System.out.println("  " + pr.extraInfo);
            }
        }
    }
    
   
    private static void printAverages(SchedulerResult result) {
        System.out.println("\nAverage Times:");
        System.out.println("-".repeat(70));
        System.out.printf("Average Waiting Time: %.2f%n", result.avgWaitingTime);
        System.out.printf("Average Turnaround Time: %.2f%n", result.avgTurnaroundTime);
    }
}
