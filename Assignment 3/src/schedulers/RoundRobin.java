package schedulers;

import models.Process;
import models.SchedulerResult;
import java.util.*;

public class RoundRobin implements Scheduler {

    class ProcessInfo {
        Process process;
        int remainingTime;
        int completionTime;
        int waitingTime;
        int turnaroundTime;

        ProcessInfo(Process p) {
            this.process = p;
            this.remainingTime = p.getBurstTime();
        }

        boolean isCompleted() {
            return remainingTime == 0;
        }
    }

    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int quantum) {
        SchedulerResult result = new SchedulerResult("Round Robin");

        Map<String, ProcessInfo> processMap = new HashMap<>();
        for (Process p : processes) {
            processMap.put(p.getName(), new ProcessInfo(p));
        }

        for (int i = 0; i < processes.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < processes.size(); j++) {
                if (processes.get(j).getArrivalTime() <
                        processes.get(minIdx).getArrivalTime()) {
                    minIdx = j;
                }
            }
            Process temp = processes.get(i);
            processes.set(i, processes.get(minIdx));
            processes.set(minIdx, temp);
        }

        Queue<ProcessInfo> queue = new LinkedList<>();
        int currentTime = 0, idx = 0, completed = 0;

        while (completed < processes.size()) {

            while (idx < processes.size() && processes.get(idx).getArrivalTime() <= currentTime) {
                queue.add(processMap.get(processes.get(idx).getName()));
                idx++;
            }

            if (queue.isEmpty()) {
                currentTime = processes.get(idx).getArrivalTime();
                continue;
            }

            ProcessInfo current = queue.poll();
            result.executionOrder.add(current.process.getName());

            int exec = Math.min(quantum, current.remainingTime);
            current.remainingTime -= exec;
            currentTime += exec;

            while (idx < processes.size() && processes.get(idx).getArrivalTime() <= currentTime) {
                queue.add(processMap.get(processes.get(idx).getName()));
                idx++;
            }

            if (current.isCompleted()) {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.process.getArrivalTime();
                current.waitingTime = current.turnaroundTime - current.process.getBurstTime();
                completed++;
            } else {
                queue.add(current);
            }

            if (!queue.isEmpty()) {
                currentTime += contextSwitchTime;
            }
        }

        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            ProcessInfo pi = processMap.get(p.getName());
            totalWT += pi.waitingTime;
            totalTAT += pi.turnaroundTime;

            result.processResults.add(
                    new SchedulerResult.ProcessResult(
                            p.getName(),
                            p.getArrivalTime(),
                            p.getBurstTime(),
                            p.getPriority(),
                            pi.waitingTime,
                            pi.turnaroundTime
                    )
            );
        }
        result.avgWaitingTime = totalWT / processes.size();
        result.avgTurnaroundTime = totalTAT / processes.size();

        return result;
    }
}
