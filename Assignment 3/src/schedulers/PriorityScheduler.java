package schedulers;

import models.Process;
import models.SchedulerResult;
import java.util.*;

public class PriorityScheduler implements Scheduler {

    class ProcessInfo { // Helper class to track process state without modifying original
        Process process;
        int remainingTime;
        int waitingTime;
        int turnaroundTime;
        int completionTime;
        int effectivePriority;  // For calculating priority with aging  (To Fix Starvation)
        int waitingStartTime;    // Track when process started waiting (To Calculate Aging)


        ProcessInfo(Process p) {
            this.process = p;
            this.remainingTime = p.getBurstTime();
            this.waitingTime = 0;
            this.turnaroundTime = 0;
            this.completionTime = 0;
            this.effectivePriority = p.getPriority();
            this.waitingStartTime = -1;
        }

        boolean isCompleted() {
            return remainingTime == 0;
        }
    }

    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int rrQuantum) {
        SchedulerResult result = new SchedulerResult("Preemptive Priority Scheduling (with Aging)");

        Map<String, ProcessInfo> processMap = new HashMap<>();
        for (Process p : processes) {
            processMap.put(p.getName(), new ProcessInfo(p));
        }

        int currentTime = 0;
        int completed = 0;
        String lastProcess = null;
        int agingThreshold = 5;

        while (completed < processes.size()) {
            // Initialize waiting time for newly arrived processes
            for (ProcessInfo pi : processMap.values()) {
                if (!pi.isCompleted() && pi.process.getArrivalTime() <= currentTime) {
                    if (pi.waitingStartTime == -1 && (lastProcess == null || !pi.process.getName().equals(lastProcess))) {
                        pi.waitingStartTime = currentTime;
                    }
                }
            }
            
            updateEffectivePriorities(processMap, currentTime, agingThreshold);

            ProcessInfo current = getHighestPriorityProcess(processMap, currentTime);

            if (current != null) {
                // Apply context switch if switching to different process
                if (lastProcess != null && !lastProcess.equals(current.process.getName())) {
                    // Old process starts waiting
                    ProcessInfo prev = processMap.get(lastProcess);
                    if (prev != null && !prev.isCompleted()) {
                        prev.waitingStartTime = currentTime;
                    }
                    currentTime += contextSwitchTime;
                }

                // Current process is now running (not waiting)
                current.waitingStartTime = -1;

                // Only add to execution order if switching
                if (lastProcess == null || !lastProcess.equals(current.process.getName())) {
                    result.executionOrder.add(current.process.getName());
                }

                // Execute for 1 time unit
                current.remainingTime--;
                currentTime++;

                // Check if completed
                if (current.isCompleted()) {
                    current.completionTime = currentTime;
                    current.turnaroundTime = current.completionTime - current.process.getArrivalTime();
                    current.waitingTime = current.turnaroundTime - current.process.getBurstTime();
                    completed++;
                }

                lastProcess = current.process.getName();
            } else {
                // CPU idle
                currentTime++;
            }
        }

        // Build results
        double totalWaiting = 0;
        double totalTurnaround = 0;
        for (Process p : processes) {
            ProcessInfo pi = processMap.get(p.getName());
            totalWaiting += pi.waitingTime;
            totalTurnaround += pi.turnaroundTime;

            SchedulerResult.ProcessResult pr = new SchedulerResult.ProcessResult(
                p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                pi.waitingTime, pi.turnaroundTime
            );
            result.processResults.add(pr);
        }

        result.avgWaitingTime = totalWaiting / processes.size();
        result.avgTurnaroundTime = totalTurnaround / processes.size();

        return result;
    }

    private void updateEffectivePriorities(Map<String, ProcessInfo> processMap, int currentTime, int agingThreshold) {
        for (ProcessInfo pi : processMap.values()) {
            if (!pi.isCompleted() && pi.process.getArrivalTime() <= currentTime) {
                if (pi.waitingStartTime == -1) {
                    // Not waiting (running or just arrived) - use base priority
                    pi.effectivePriority = pi.process.getPriority();
                } else {
                    // Waiting - apply aging
                    int waitingDuration = currentTime - pi.waitingStartTime;
                    pi.effectivePriority = pi.process.getPriority() - (waitingDuration / agingThreshold);
                }
            }
        }
    }

    private ProcessInfo getHighestPriorityProcess(Map<String, ProcessInfo> processMap, int currentTime) {
        ProcessInfo highest = null;

        for (ProcessInfo pi : processMap.values()) {
            if (pi.process.getArrivalTime() <= currentTime && !pi.isCompleted()) {
                if (highest == null) {
                    highest = pi;
                } else if (pi.effectivePriority < highest.effectivePriority) {
                    highest = pi;
                } else if (pi.effectivePriority == highest.effectivePriority) {
                    // Tie-break 1: Base priority (lower is better)
                    if (pi.process.getPriority() < highest.process.getPriority()) {
                        highest = pi;
                    } else if (pi.process.getPriority() == highest.process.getPriority()) {
                        // Tie-break 2: Waiting time (longer waiting wins)
                        int piWait = (pi.waitingStartTime == -1) ? 0 : currentTime - pi.waitingStartTime;
                        int highWait = (highest.waitingStartTime == -1) ? 0 : currentTime - highest.waitingStartTime;
                        
                        if (piWait > highWait) {
                            highest = pi;
                        } else if (piWait == highWait) {
                            // Tie-break 3: Arrival time (earlier is better)
                            if (pi.process.getArrivalTime() < highest.process.getArrivalTime()) {
                                highest = pi;
                            } else if (pi.process.getArrivalTime() == highest.process.getArrivalTime()) {
                                // Tie-break 4: Name (alphabetical)
                                if (pi.process.getName().compareTo(highest.process.getName()) < 0) {
                                    highest = pi;
                                }
                            }
                        }
                    }
                }
            }
        }

        return highest;
    }
}
