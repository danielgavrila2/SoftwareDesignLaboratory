package com.fitness.aiservice;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.ActivityAIService;
import com.fitness.aiservice.service.GeminiService;
import com.fitness.aiservice.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ActivityAIServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private ActivityAIService activityAIService;

    @Test
    public void testGenerateRecommendation_Success() {
        Activity activity = new Activity();
        activity.setId("act123");
        activity.setUserId("user123");
        activity.setDuration(45);
        activity.setCaloriesBurned(400);
        activity.setAdditionalMetrics(new HashMap<>());

        String mockWeatherContext = "Sunny, 25C";
        String mockAiResponse = "```json\n{\n" +
                "  \"analysis\": {\n" +
                "    \"overall\": \"Great job.\",\n" +
                "    \"pace\": \"Good pace.\",\n" +
                "    \"heartRate\": \"Normal.\",\n" +
                "    \"caloriesBurned\": \"High.\"\n" +
                "  },\n" +
                "  \"improvements\": [\n" +
                "    {\n" +
                "      \"area\": \"Endurance\",\n" +
                "      \"recommendation\": \"Run longer.\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"suggestions\": [\n" +
                "    {\n" +
                "      \"workout\": \"Intervals\",\n" +
                "      \"description\": \"Do sprints.\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"safety\": [\n" +
                "    \"Stay hydrated.\"\n" +
                "  ],\n" +
                "  \"dailyPlan\": []\n" +
                "}\n```";

        Mockito.when(weatherService.getWeeklyForecastContext()).thenReturn(mockWeatherContext);
        Mockito.when(geminiService.getAnswer(anyString())).thenReturn(mockAiResponse);

        Recommendation recommendation = activityAIService.generateRecommendation(activity);

        assertNotNull(recommendation);
        assertEquals("act123", recommendation.getActivityId());
        assertEquals("user123", recommendation.getUserId());
        assertTrue(recommendation.getRecommendation().contains("Great job"));
    }
}
