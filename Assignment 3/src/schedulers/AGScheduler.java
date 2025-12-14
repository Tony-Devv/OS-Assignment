package schedulers;

import models.Process;
import models.SchedulerResult;
import java.util.List;

public class AGScheduler implements Scheduler {
    
    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int rrQuantum) {
        SchedulerResult result = new SchedulerResult("AG Scheduling");
        
   
        
        return result;
    }
}
