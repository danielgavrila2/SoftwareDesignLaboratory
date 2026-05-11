package com.fitness.activityservice.pattern.template;

import com.fitness.activityservice.sto.ActivityResponse;
import java.util.List;

public abstract class BaseExportTemplate {
    // Template Method
    public final String export(List<ActivityResponse> activities) {
        List<ActivityResponse> data = fetchData(activities);
        String transformedData = transform(data);
        return writeOutput(transformedData);
    }
    
    protected List<ActivityResponse> fetchData(List<ActivityResponse> activities) {
        // Common logic, simply returning data for now
        return activities;
    }
    
    protected abstract String transform(List<ActivityResponse> data);
    protected abstract String writeOutput(String transformedData);
}
