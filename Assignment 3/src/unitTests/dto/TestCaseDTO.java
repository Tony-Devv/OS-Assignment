package unitTests.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCaseDTO {
    public String name;
    public InputDTO input;
    // JsonNode for expectedOutput because it differs between AG (Single Object) and Standard (Map of Objects)
    public JsonNode expectedOutput;

    public static class InputDTO {
        public int contextSwitch;
        public int rrQuantum;
        public int agingInterval;
        public List<ProcessDTO> processes;
    }
}