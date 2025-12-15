package schedulers;

import models.Process;
import models.SchedulerResult;
import models.SchedulerResult.ProcessResult;
import java.util.*;

public class SJFPreemptive implements Scheduler {

    @Override
    public SchedulerResult schedule(List<Process> processes, int contextSwitchTime, int rrQuantum) {
        SchedulerResult result = new SchedulerResult("Preemptive Shortest Job First (SJF)");

        // Copy and sort processes by arrival time
        List<Process> procList = new ArrayList<>(processes);
        procList.sort(Comparator.comparingInt(Process::getArrivalTime));

        int n = procList.size();

        // Create arrays of process data attributes
        String[] names = new String[n];
        int[] arrival = new int[n];
        int[] burst = new int[n];
        int[] remaining = new int[n];
        int[] finish = new int[n];
        int[] start = new int[n];
        Arrays.fill(start, -1); // Initialize start times to -1

        for (int i = 0; i < n; i++) { // Copy process data
            Process p = procList.get(i);
            names[i] = p.getName();
            arrival[i] = p.getArrivalTime();
            burst[i] = p.getBurstTime();
            remaining[i] = burst[i];
        }

        // Variables for simulation
        int time = 0;
        int completed = 0;
        List<String> execution_order = new ArrayList<>();
        int current_index = -1;
        int last_index = -1;

        while (completed < n) { // Loop until all processes are completed
            // Find the ready process with shortest remaining time
            int min_remaining = -1;
            int shortest_index = -1;

            for (int i = 0; i < n; i++) { // Find process with shortest remaining time
                if (arrival[i] <= time && remaining[i] > 0) {
                    if (shortest_index == -1) { // If this is the first process found
                        min_remaining = remaining[i];
                        shortest_index = i;
                    } else if (remaining[i] < min_remaining) {
                        // Found shorter process
                        min_remaining = remaining[i];
                        shortest_index = i;
                    } else if (remaining[i] == min_remaining) { // If same remaining time
                        if (arrival[i] < arrival[shortest_index]) { // Choose process with earlier arrival
                            shortest_index = i;
                        }
                    }
                }
            }

            // If no process found
            if (shortest_index == -1) {
                time++;
                continue;
            }

            // Context switch if process changed
            if (last_index != -1 && last_index != shortest_index) {
                time += contextSwitchTime;
            }

            // Record start time if first time running
            if (start[shortest_index] == -1) {
                start[shortest_index] = time;
            }

            // Add to execution order
            if (current_index != shortest_index) {
                execution_order.add(names[shortest_index]);
                current_index = shortest_index;
            }

            // Execute one time unit
            remaining[shortest_index]--;
            time++;

            // If process finished
            if (remaining[shortest_index] == 0) {
                finish[shortest_index] = time;
                completed++;
                current_index = -1;
            }

            // Update last index for next iteration
            last_index = shortest_index;
        }

        // Set execution order
        result.executionOrder = execution_order;

        // Calculate Waiting Time and Turnaround Time
        double total_waiting_time = 0;
        double total_turnaround_time = 0;

        for (int i = 0; i < n; i++) {
            // Turnaround Time = Finish time - Arrival time
            int turnaround_time = finish[i] - arrival[i];

            // Waiting Time = Turnaround Time - Burst time
            int waiting_time = turnaround_time - burst[i];

            total_waiting_time += waiting_time;
            total_turnaround_time += turnaround_time;

            // Add process result to result
            result.processResults.add(new ProcessResult(
                    names[i],
                    arrival[i],
                    burst[i],
                    0, // Priority not used in SJF
                    waiting_time,
                    turnaround_time
            ));
        }

        // Calculate averages waiting and turnaround time rounded to 2 decimal places
        result.avgWaitingTime = Math.round((total_waiting_time / n) * 100.0) / 100.0;
        result.avgTurnaroundTime = Math.round((total_turnaround_time / n) * 100.0) / 100.0;

        return result;
    }
}
