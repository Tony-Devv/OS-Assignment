package utils;

import models.Process;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles user input for the CPU Scheduler Simulator
 * 
 * ASSIGNED TO: P4 & P5 (Integration)
 */
public class InputHandler {
    private Scanner scanner;
    
    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Get number of processes from user
     */
    public int getNumberOfProcesses() {

        System.out.print("Enter number of processes: ");
        
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Enter number of processes: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
    
    /**
     * Get Round Robin time quantum from user
     */
    public int getRoundRobinQuantum() {
        System.out.print("Enter Round Robin time quantum: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Enter time quantum: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
    
    /**
     * Get context switching time from user
     */
    public int getContextSwitchTime() {
        System.out.print("Enter context switching time: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Enter context switching time: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
    
   
    /**
     * Get process details from user (always includes quantum)
     */
    public List<Process> getProcesses(int count, boolean includeQuantum) {
        List<Process> processes = new ArrayList<>();
        
        System.out.println("\nEnter process details:");
        for (int i = 0; i < count; i++) {
            System.out.println("\n--- Process " + (i + 1) + " ---");
            
            System.out.print("Process Name: ");
            String name = scanner.next();
            
            System.out.print("Arrival Time: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter arrival time: ");
                scanner.next();
            }
            int arrivalTime = scanner.nextInt();
            
            System.out.print("Burst Time: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter burst time: ");
                scanner.next();
            }
            int burstTime = scanner.nextInt();
            
            System.out.print("Priority: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter priority: ");
                scanner.next();
            }
            int priority = scanner.nextInt();
            
            System.out.print("Quantum: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter quantum: ");
                scanner.next();
            }
            int quantum = scanner.nextInt();
            
            processes.add(new Process(name, arrivalTime, burstTime, priority, quantum));
        }
        
        return processes;
    }
    
    public void close() {
        scanner.close();
    }
}
