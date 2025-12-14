package models;


 public class Process {
    private String name;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int quantum;  // For AG Scheduling
    private StringBuilder quantumHistory;
    
    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
    }

    public String getName() {
        return name;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public int getBurstTime() {
        return burstTime;
    }
    public int getPriority() {
        return priority;
    }
    public int getQuantum() {
        return quantum;
    }
    

    
}
