import models.Process;
import models.SchedulerResult;
import schedulers.*;
import utils.InputHandler;
import utils.OutputFormatter;
import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        InputHandler input = new InputHandler();
        
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       CPU SCHEDULER SIMULATOR - Assignment #3        ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();
        
        int numProcesses = input.getNumberOfProcesses();
        int rrQuantum = input.getRoundRobinQuantum();
        int contextSwitchTime = input.getContextSwitchTime();
        List<Process> processes = input.getProcesses(numProcesses, true);
        
        System.out.println("\n=== Running All Schedulers ===\n");
        
        SJFPreemptive sjf = new SJFPreemptive();
        SchedulerResult sjfResult = sjf.schedule(processes, contextSwitchTime, rrQuantum);
        OutputFormatter.printResult(sjfResult);
        
        RoundRobin rr = new RoundRobin();
        SchedulerResult rrResult = rr.schedule(processes, contextSwitchTime, rrQuantum);
        OutputFormatter.printResult(rrResult);
        
        PriorityScheduler priority = new PriorityScheduler();
        SchedulerResult priorityResult = priority.schedule(processes, contextSwitchTime, rrQuantum);
        OutputFormatter.printResult(priorityResult);
        
        AGScheduler ag = new AGScheduler();
        SchedulerResult agResult = ag.schedule(processes, contextSwitchTime, rrQuantum);
        OutputFormatter.printResult(agResult);
        
        input.close();
    }
}
