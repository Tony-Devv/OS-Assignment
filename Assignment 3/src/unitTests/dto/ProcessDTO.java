package unitTests.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessDTO {
    public String name;
    public int arrival;
    public int burst;
    public int priority;
    public int quantum;
}