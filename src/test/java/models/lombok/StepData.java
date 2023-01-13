package models.lombok;

import lombok.Data;

@Data
public class StepData {
    private String name;
    private boolean hasContent , leaf;
    private int stepsCount;

}
