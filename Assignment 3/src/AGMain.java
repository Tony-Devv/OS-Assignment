import models.Process;
import models.SchedulerResult;
import schedulers.AGScheduler;
import utils.InputHandler;
import utils.OutputFormatter;
import java.util.List;

public class AGMain {
    public static void main(String[] args) {
        InputHandler input = new InputHandler();

        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║            AG CPU SCHEDULER - Assignment #3          ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();

        int numProcesses = input.getNumberOfProcesses();
        // int rrQuantum = input.getRoundRobinQuantum();
        // int contextSwitchTime = input.getContextSwitchTime();
        List<Process> processes = input.getProcesses(numProcesses, true);

        AGScheduler ag = new AGScheduler();
        SchedulerResult agResult = ag.schedule(processes, 0, 4);
        OutputFormatter.printResult(agResult);

        input.close();
    }
}
