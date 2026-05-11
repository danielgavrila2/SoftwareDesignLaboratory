package com.fitness.activityservice.pattern.template;

import com.fitness.activityservice.sto.ActivityResponse;
import java.util.List;

public class CsvExport extends BaseExportTemplate {
    @Override
    protected String transform(List<ActivityResponse> data) {
        StringBuilder sb = new StringBuilder("ID,UserId,Type,Duration,Calories\\n");
        for(ActivityResponse a : data) {
            sb.append(a.getId()).append(",")
              .append(a.getUserId()).append(",")
              .append(a.getActivityType()).append(",")
              .append(a.getDuration()).append(",")
              .append(a.getCaloriesBurned()).append("\\n");
        }
        return sb.toString();
    }

    @Override
    protected String writeOutput(String transformedData) {
        return transformedData;
    }
}
