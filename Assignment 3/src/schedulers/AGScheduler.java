package schedulers;

import models.Process;
import models.SchedulerResult;
import java.util.*;

public class AGScheduler implements Scheduler {

    private static class AGProcessInfo {
        Process process;
        int remainingTime;
        int quantum;
        int completionTime;
        int waitingTime;
        int turnaroundTime;
        List<Integer> quantumHistory = new ArrayList<>();
        int currentQuantumUsed = 0;

        AGProcessInfo(Process p) {
            this.process = p;
            this.remainingTime = p.getBurstTime();
            this.quantum = p.getQuantum();
            this.quantumHistory.add(this.quantum);
        }

        boolean isFinished() {
            return remainingTime <= 0;
        }
    }

    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int rrQuantum) {
        SchedulerResult result = new SchedulerResult("AG Scheduling");
        if (processes == null || processes.isEmpty()) return result;

        // 1. Setup
        List<Process> sortedList = new ArrayList<>(processes);
        sortedList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        Map<String, AGProcessInfo> infoMap = new HashMap<>();
        for (Process p : sortedList) infoMap.put(p.getName(), new AGProcessInfo(p));

        Deque<AGProcessInfo> readyQueue = new LinkedList<>();
        int time = 0;
        int completed = 0;
        int arrivalIndex = 0;
        int n = sortedList.size();

        // Initial arrival
        if (arrivalIndex < n) {
            time = Math.max(0, sortedList.get(arrivalIndex).getArrivalTime());
            while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                arrivalIndex++;
            }
        }

        AGProcessInfo lastProcess = null;

        // 2. Main Loop
        while (completed < n) {
            // Fill empty queue if gaps exist in arrival times
            if (readyQueue.isEmpty() && arrivalIndex < n) {
                time = Math.max(time, sortedList.get(arrivalIndex).getArrivalTime());
                while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                    readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                    arrivalIndex++;
                }
            }
            
            if (readyQueue.isEmpty()) break;

            AGProcessInfo current = readyQueue.poll();
            
            // Context Switch logic (usually 0 )
            if (lastProcess != null && current != lastProcess && contextSwitchTime > 0) {
                time += contextSwitchTime; 
                // Add arrivals during CS
                while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                    readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                    arrivalIndex++;
                }
            }

            result.executionOrder.add(current.process.getName());
            
            // Reset run stats
            current.currentQuantumUsed = 0;
            int Q = current.quantum;

            // t1 = End of FCFS (25%)
            // t2 = End of Priority (Next 25%, so Cumulative 25% + 25%)
            int t1 = (int) Math.ceil(Q * 0.25);
            int t2 = t1 + (int) Math.ceil(Q * 0.25); 

            boolean preempted = false;
            String scenario = "";
            AGProcessInfo nextToRun = null;
            
            // ==========================================
            // Phase 1: FCFS
            // ==========================================
            while (current.currentQuantumUsed < t1 && !current.isFinished()) {
                current.remainingTime--;
                current.currentQuantumUsed++;
                time++;
                
                // Check arrivals
                while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                    readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                    arrivalIndex++;
                }
            }

            if (current.isFinished()) {
                finishProcess(current, time);
                completed++;
                lastProcess = current;
                continue;
            }

            // ==========================================
            // Phase 2: Priority 
            // ==========================================
            
            // Check if we should preempt NOW before running Priority Phase
            AGProcessInfo bestPrio = getBestPriorityProcess(readyQueue);
            if (bestPrio != null && bestPrio.process.getPriority() < current.process.getPriority()) {
                preempted = true;
                scenario = "ii";
                nextToRun = bestPrio;
            } else {
                // Run Priority Phase
                while (current.currentQuantumUsed < t2 && !current.isFinished()) {
                    current.remainingTime--;
                    current.currentQuantumUsed++;
                    time++;
                    
                    while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                        readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                        arrivalIndex++;
                    }
                }
            }

            if (!preempted && current.isFinished()) {
                finishProcess(current, time);
                completed++;
                lastProcess = current;
                continue;
            }

            // ==========================================
            // Phase 3: SJF (Preemptive)
            // ==========================================
            if (!preempted) {
                // Check if we should preempt NOW before running SJF Phase
                AGProcessInfo shortest = getShortestJobProcess(readyQueue, current);
                
                if (shortest != null && shortest != current) {
                    preempted = true;
                    scenario = "iii";
                    nextToRun = shortest;
                } else {
                    // Run SJF Phase
                    while (current.currentQuantumUsed < Q && !current.isFinished()) {
                        current.remainingTime--;
                        current.currentQuantumUsed++;
                        time++;

                        while (arrivalIndex < n && sortedList.get(arrivalIndex).getArrivalTime() <= time) {
                            readyQueue.add(infoMap.get(sortedList.get(arrivalIndex).getName()));
                            arrivalIndex++;
                        }
                        
                        // In Strictly Preemptive SJF, we check every unit
                        AGProcessInfo newerShortest = getShortestJobProcess(readyQueue, current);
                        if (newerShortest != null && newerShortest != current) {
                            preempted = true;
                            scenario = "iii";
                            nextToRun = newerShortest;
                            break;
                        }
                    }
                }
            }

            // ==========================================
            // Completion / Updates
            // ==========================================
            if (!current.isFinished()) {
                // Update Quantum
                if (preempted) {
                    if (scenario.equals("ii")) {
                        // Scenario ii: Priority Preemption -> Q += ceil((Q - Used)/2)
                        int remQ = current.quantum - current.currentQuantumUsed;
                        current.quantum += (int) Math.ceil(remQ / 2.0);
                    } else if (scenario.equals("iii")) {
                        // Scenario iii: SJF Preemption -> Q += (Q - Used)
                        int remQ = current.quantum - current.currentQuantumUsed;
                        current.quantum += remQ;
                    }
                } else {
                    // Scenario i: Used all Q -> Q += 2
                    current.quantum += 2;
                }
                current.quantumHistory.add(current.quantum);
                
                // Logic for Queue Management on Preemption
                // 1. Current goes to TAIL
                readyQueue.add(current);
                
                // 2. If preempted, the one that caused preemption must be at HEAD
                if (preempted && nextToRun != null) {
                    readyQueue.remove(nextToRun); // Remove from wherever it is
                    readyQueue.addFirst(nextToRun); // Move to front
                }
            } else {
                finishProcess(current, time);
                completed++;
            }
            
            lastProcess = current;
        }

        fillResult(result, infoMap, sortedList);
        return result;
    }

    private void finishProcess(AGProcessInfo p, int time) {
        p.completionTime = time;
        p.turnaroundTime = p.completionTime - p.process.getArrivalTime();
        p.waitingTime = p.turnaroundTime - p.process.getBurstTime();
        p.quantum = 0;
        p.quantumHistory.add(0);
    }

    private AGProcessInfo getBestPriorityProcess(Queue<AGProcessInfo> queue) {
        AGProcessInfo best = null;
        for (AGProcessInfo p : queue) {
            if (best == null || p.process.getPriority() < best.process.getPriority()) {
                best = p;
            }
        }
        return best;
    }

    private AGProcessInfo getShortestJobProcess(Queue<AGProcessInfo> queue, AGProcessInfo current) {
        AGProcessInfo best = current;
        for (AGProcessInfo p : queue) {
            if (p.remainingTime < best.remainingTime) {
                best = p;
            }
        }
        return (best == current) ? null : best; // Return null if current is still shortest
    }

    private void fillResult(SchedulerResult result, Map<String, AGProcessInfo> map, List<Process> originals) {
        double totalWT = 0;
        double totalTAT = 0;
        for (Process p : originals) {
            AGProcessInfo info = map.get(p.getName());
            totalWT += info.waitingTime;
            totalTAT += info.turnaroundTime;
            
            SchedulerResult.ProcessResult pr = new SchedulerResult.ProcessResult(
                p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                info.waitingTime, info.turnaroundTime
            );
            pr.extraInfo = "Quantum history: " + info.quantumHistory;
            result.processResults.add(pr);
        }
        result.avgWaitingTime = totalWT / originals.size();
        result.avgTurnaroundTime = totalTAT / originals.size();
    }
}
