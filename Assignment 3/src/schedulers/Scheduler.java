package schedulers;

import models.Process;
import models.SchedulerResult;
import java.util.List;

public interface Scheduler {
    SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int rrQuantum);
}
