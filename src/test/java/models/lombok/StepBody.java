package models.lombok;

import lombok.Data;

import java.util.List;

@Data
public class StepBody {
    private List<StepData> steps;
}