package models;

import java.util.ArrayList;
import java.util.List;

public class SchedulerResult {
    public String schedulerName;
    public List<String> executionOrder;
    public List<ProcessResult> processResults;
    public double avgWaitingTime;
    public double avgTurnaroundTime;
    
    public SchedulerResult(String schedulerName) {
        this.schedulerName = schedulerName;
        this.executionOrder = new ArrayList<>();
        this.processResults = new ArrayList<>();
        this.avgWaitingTime = 0.0;
        this.avgTurnaroundTime = 0.0;
    }
    
    public static class ProcessResult {
        public String name;
        public int arrivalTime;
        public int burstTime;
        public int priority;
        public int waitingTime;
        public int turnaroundTime;
        public String extraInfo;  // For AG quantum history, or anything else
        
        public ProcessResult(String name, int arrival, int burst, int priority, int waiting, int turnaround) {
            this.name = name;
            this.arrivalTime = arrival;
            this.burstTime = burst;
            this.priority = priority;
            this.waitingTime = waiting;
            this.turnaroundTime = turnaround;
            this.extraInfo = "";
        }
    }
}
