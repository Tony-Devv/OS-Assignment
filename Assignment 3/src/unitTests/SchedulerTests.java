package unitTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Process;
import models.SchedulerResult;
import models.SchedulerResult.ProcessResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import schedulers.*;
import unitTests.dto.TestCaseDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SchedulerTests {

    private final ObjectMapper mapper = new ObjectMapper();

    // ==========================================
    // AG SCHEDULING TESTS
    // ==========================================

    @Test
    public void testAG_Case1() throws IOException { runAGTest("AG/AG_test1.json"); }
    @Test
    public void testAG_Case2() throws IOException { runAGTest("AG/AG_test2.json"); }

    @Test
    public void testAG_Case3() throws IOException { runAGTest("AG/AG_test3.json"); }

    @Test
    public void testAG_Case4() throws IOException { runAGTest("AG/AG_test4.json"); }

    @Test
    public void testAG_Case5() throws IOException { runAGTest("AG/AG_test5.json"); }

    @Test
    public void testAG_Case6() throws IOException { runAGTest("AG/AG_test6.json"); }

    // ==========================================
    // STANDARD SCHEDULER TESTS (SJF, RR, Priority)
    // ==========================================

    @Test
    public void testStandard_Case1() throws IOException { runStandardTest("test_1.json"); }

    @Test
    public void testStandard_Case2() throws IOException { runStandardTest("test_2.json"); }

    @Test
    public void testStandard_Case3() throws IOException { runStandardTest("test_3.json"); }

    @Test
    public void testStandard_Case4() throws IOException { runStandardTest("test_4.json"); }

    @Test
    public void testStandard_Case5() throws IOException { runStandardTest("test_5.json"); }

    @Test
    public void testStandard_Case6() throws IOException { runStandardTest("test_6.json"); }


    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void runAGTest(String filename) throws IOException {
        // 1. Parse Inputs
        TestCaseDTO testCase = TestLoader.loadTestCase(filename);
        List<Process> processes = TestLoader.mapToDomain(testCase.input.processes);

        // 2. Run Schedule
        AGScheduler scheduler = new AGScheduler();

        // Context switch and RR Quantum are usually 0 or irrelevant for AG logic unless specified
        SchedulerResult result = scheduler.schedule(processes, 0, 0);

        // 3. Assert
        assertSchedulerResult(testCase.expectedOutput, result);
    }

    private void runStandardTest(String filename) throws IOException {
        TestCaseDTO testCase = TestLoader.loadTestCase(filename);
        int cs = testCase.input.contextSwitch;
        int rrQ = testCase.input.rrQuantum;
        int agingInterval = testCase.input.agingInterval;

        // Test SJF
        if (testCase.expectedOutput.has("SJF")) {
            List<Process> processes = TestLoader.mapToDomain(testCase.input.processes);
            SJFPreemptive sjf = new SJFPreemptive();
            SchedulerResult result = sjf.schedule(processes, cs, rrQ);
            assertSchedulerResult(testCase.expectedOutput.get("SJF"), result);
        }

        // Test Round Robin
        if (testCase.expectedOutput.has("RR")) {
            List<Process> processes = TestLoader.mapToDomain(testCase.input.processes);
            RoundRobin rr = new RoundRobin();
            SchedulerResult result = rr.schedule(processes, cs, rrQ);
            assertSchedulerResult(testCase.expectedOutput.get("RR"), result);
        }

        // Test Priority
        if (testCase.expectedOutput.has("Priority")) {
            List<Process> processes = TestLoader.mapToDomain(testCase.input.processes);
            PriorityScheduler priority = new PriorityScheduler();
            SchedulerResult result = priority.schedule(processes, cs, agingInterval);
            assertSchedulerResult(testCase.expectedOutput.get("Priority"), result);
        }
    }

    private void assertSchedulerResult(JsonNode expected, SchedulerResult actual) {
        // 1. Assert Execution Order
        List<String> expectedOrder = mapper.convertValue(expected.get("executionOrder"), List.class);
        Assertions.assertEquals(expectedOrder, actual.executionOrder,
                "Execution Order Mismatch for " + actual.schedulerName);

        // 2. Assert Process Details (Wait Time, Turnaround Time, History)
        JsonNode expectedProcessResults = expected.get("processResults");
        for (JsonNode expProc : expectedProcessResults) {
            String pName = expProc.get("name").asText();
            int expWait = expProc.get("waitingTime").asInt();
            int expTurn = expProc.get("turnaroundTime").asInt();

            // Find the actual process result
            Optional<ProcessResult> actProcOpt = actual.processResults.stream()
                    .filter(p -> p.name.equals(pName))
                    .findFirst();

            Assertions.assertTrue(actProcOpt.isPresent(), "Process " + pName + " missing in output");
            ProcessResult actProc = actProcOpt.get();

            Assertions.assertEquals(expWait, actProc.waitingTime,
                    "Waiting Time mismatch for process " + pName + " in " + actual.schedulerName);
            Assertions.assertEquals(expTurn, actProc.turnaroundTime,
                    "Turnaround Time mismatch for process " + pName + " in " + actual.schedulerName);

            // Special Check for AG Scheduling: Quantum History
            if (expProc.has("quantumHistory")) {
                String expectedHistory = expProc.get("quantumHistory").toString().replace(" ", "");
                String actualHistory = actProc.extraInfo.replace(" ", ""); // output is usually "Quantum history: [...]"

                Assertions.assertTrue(actualHistory.contains(expectedHistory),
                        "Quantum History mismatch for " + pName +
                                "\nExpected to contain: " + expectedHistory +
                                "\nActual output: " + actualHistory);
            }
        }

        // 3. Assert Averages
        double expAvgWait = expected.get("averageWaitingTime").asDouble();
        double expAvgTurn = expected.get("averageTurnaroundTime").asDouble();

        Assertions.assertEquals(expAvgWait, actual.avgWaitingTime, 0.01,
                "Average Waiting Time Mismatch for " + actual.schedulerName);
        Assertions.assertEquals(expAvgTurn, actual.avgTurnaroundTime, 0.01,
                "Average Turnaround Time Mismatch for " + actual.schedulerName);
    }
}