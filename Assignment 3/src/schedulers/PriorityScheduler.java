package schedulers;

import java.util.*;
import models.Process;
import models.SchedulerResult;

public class PriorityScheduler implements Scheduler {

    class ProcessInfo {
        Process process;
        int remainingTime;
        int completionTime;
        int waitingTime;
        int priority;
        boolean added;
        int order;

        ProcessInfo(Process p, int order) {
            this.process = p;
            this.remainingTime = p.getBurstTime();
            this.completionTime = 0;
            this.waitingTime = 0;
            this.priority = p.getPriority();
            this.added = false;
            this.order = order;
        }

        boolean isDone() {
            return remainingTime == 0;
        }
    }

    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int agingInterval) {
        SchedulerResult result = new SchedulerResult("Preemptive Priority Scheduling (with Aging)");

        Map<String, ProcessInfo> processMap = new HashMap<>();
        int idx = 0;
        for (Process p : processes) {
            processMap.put(p.getName(), new ProcessInfo(p, idx++));
        }

        int time = 0;
        int done = 0;
        ProcessInfo CurrentProcess = null;
        ProcessInfo PreviousProcess = null;

        while (done < processes.size()) {
            
            // Check for new arrivals
            for (ProcessInfo pi : processMap.values()) {
                if (!pi.added && pi.process.getArrivalTime() == time) {
                    pi.added = true;
                }
            }
            
            // Update execution order
            if (CurrentProcess != null && (result.executionOrder.isEmpty() || 
                !result.executionOrder.get(result.executionOrder.size() - 1).equals(CurrentProcess.process.getName()))) {
                result.executionOrder.add(CurrentProcess.process.getName());
            }
            
            // Pick a process if nothing is running
            if (CurrentProcess == null) {
                CurrentProcess = GetBestProcess(processMap, time);
                
                if (CurrentProcess != null) {
                    if (PreviousProcess != null && !PreviousProcess.process.getName().equals(CurrentProcess.process.getName()) && time != 0) {
                        doContextSwitch(processMap, CurrentProcess, contextSwitchTime, agingInterval, time);
                        time += contextSwitchTime;
                        continue;
                    }
                    PreviousProcess = CurrentProcess;
                }
            }
            
            // Check for preemption
            if (CurrentProcess != null) {
                ProcessInfo BetterPriority = GetBestProcess(processMap, time);
                
                if (BetterPriority != null && !BetterPriority.process.getName().equals(CurrentProcess.process.getName())) {
                    if (BetterPriority.priority < CurrentProcess.priority ||
                        (BetterPriority.priority == CurrentProcess.priority && BetterPriority.process.getArrivalTime() < CurrentProcess.process.getArrivalTime()) ||
                        (BetterPriority.priority == CurrentProcess.priority && BetterPriority.process.getArrivalTime() == CurrentProcess.process.getArrivalTime() &&
                         BetterPriority.order < CurrentProcess.order)) {
                        
                        PreviousProcess = CurrentProcess;
                        CurrentProcess = BetterPriority;
                        
                        doContextSwitch(processMap, CurrentProcess, contextSwitchTime, agingInterval, time);
                        time += contextSwitchTime;
                        continue;
                    }
                }
                CurrentProcess.waitingTime = 0;
            }
            
            // Execute
            if (CurrentProcess != null) {
                CurrentProcess.remainingTime--;
                
                if (CurrentProcess.isDone()) {
                    CurrentProcess.completionTime = time + 1;
                    done++;
                    PreviousProcess = CurrentProcess;
                    CurrentProcess = null;
                }
            }
            
            // Update waiting times
            for (ProcessInfo pi : processMap.values()) {
                if (pi.added && !pi.isDone() && 
                    (CurrentProcess == null || !pi.process.getName().equals(CurrentProcess.process.getName()))) {
                    pi.waitingTime++;
                }
            }
            
            time++;
            CheckIfAgingAppropriate(processMap, agingInterval);
        }

        // Calculate metrics
        double totalWaiting = 0;
        double totalTurnaround = 0;
        
        for (Process p : processes) {
            ProcessInfo pi = processMap.get(p.getName());
            int turnaroundTime = pi.completionTime - p.getArrivalTime();
            int waitingTime = turnaroundTime - p.getBurstTime();
            
            totalWaiting += waitingTime;
            totalTurnaround += turnaroundTime;

            result.processResults.add(new SchedulerResult.ProcessResult(
                p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                waitingTime, turnaroundTime
            ));
        }

        result.avgWaitingTime = totalWaiting / processes.size();
        result.avgTurnaroundTime = totalTurnaround / processes.size();

        return result;
    }

    private void doContextSwitch(Map<String, ProcessInfo> processMap, ProcessInfo next,
                                 int contextSwitchTime, int agingInterval, int currentTime) {
        for (int i = 0; i < contextSwitchTime; i++) {
            if (next != null) {
                next.waitingTime++;
            }
            
            for (ProcessInfo pi : processMap.values()) {
                if (pi.added && !pi.isDone() && 
                    (next == null || !pi.process.getName().equals(next.process.getName()))) {
                    pi.waitingTime++;
                }
            }
            
            for (ProcessInfo pi : processMap.values()) {
                if (!pi.added && pi.process.getArrivalTime() == (currentTime + i + 1)) {
                    pi.added = true;
                }
            }
            
            CheckIfAgingAppropriate(processMap, agingInterval);
        }
    }

    private void CheckIfAgingAppropriate(Map<String, ProcessInfo> processMap, int agingInterval) {
        if (agingInterval == 0) return;
        
        for (ProcessInfo pi : processMap.values()) {
            if (pi.added && !pi.isDone() && pi.waitingTime >= agingInterval) {
                pi.priority = Math.max(1, pi.priority - 1);
                pi.waitingTime = 0;
            }
        }
    }

    private ProcessInfo GetBestProcess(Map<String, ProcessInfo> processMap, int currentTime) {
        ProcessInfo best = null;

        for (ProcessInfo pi : processMap.values()) {
            if (pi.added && pi.process.getArrivalTime() <= currentTime && !pi.isDone()) {
                if (best == null) {
                    best = pi;
                } else {
                    int curPriority = pi.priority;
                    int bestPriority = best.priority;
                    
                    if (curPriority < bestPriority) {
                        best = pi;
                    } else if (curPriority == bestPriority) {
                        if (pi.process.getArrivalTime() < best.process.getArrivalTime()) {
                            best = pi;
                        } else if (pi.process.getArrivalTime() == best.process.getArrivalTime()) {
                            if (pi.order < best.order) {
                                best = pi;
                            }
                        }
                    }
                }
            }
        }

        return best;
    }
}
