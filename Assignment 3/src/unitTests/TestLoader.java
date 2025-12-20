package unitTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Process;
import unitTests.dto.ProcessDTO;
import unitTests.dto.TestCaseDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestLoader {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TEST_DIR = "TestCases/";

    public static TestCaseDTO loadTestCase(String filename) throws IOException {
        return mapper.readValue(new File(TEST_DIR + filename), TestCaseDTO.class);
    }


    public static List<Process> mapToDomain(List<ProcessDTO> dtos) {
        List<Process> processes = new ArrayList<>();
        for (ProcessDTO dto : dtos) {
            processes.add(new Process(
                    dto.name,
                    dto.arrival,
                    dto.burst,
                    dto.priority,
                    dto.quantum
            ));
        }
        return processes;
    }
}