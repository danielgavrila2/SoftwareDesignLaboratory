package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.DailyPlan;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;
    private final WeatherService weatherService;
    private final RestTemplate restTemplate = new RestTemplate();

    public Recommendation generateRecommendation(Activity activity) {
        String weatherContext = weatherService.getWeeklyForecastContext();
        String userProfile = fetchUserProfile(activity.getUserId());
        String prompt = createPromptForActivity(activity, weatherContext, userProfile);
        log.info("Sending prompt to Gemini API with weather and profile context..."); 
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("Received response from Gemini API."); 
        log.info("RESPONSE FROM AI: {}", aiResponse);

        return processAiResponse(activity, aiResponse);
    }

    private String fetchUserProfile(String userId) {
        try {
            String url = "http://localhost:8081/api/users/" + userId;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null) {
                StringBuilder profile = new StringBuilder();
                profile.append("Athlete Profile:\n");
                if (response.has("height") && !response.get("height").isNull()) profile.append("- Height: ").append(response.get("height").asDouble()).append(" cm\n");
                if (response.has("weight") && !response.get("weight").isNull()) profile.append("- Weight: ").append(response.get("weight").asDouble()).append(" kg\n");
                if (response.has("age") && !response.get("age").isNull()) profile.append("- Age: ").append(response.get("age").asInt()).append(" years\n");
                return profile.toString();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user profile: {}", e.getMessage());
        }
        return "Athlete Profile: Not available\n";
    }


    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```", "")
                    .trim();

//            log.info("PARSED RESPONSE FROM AI: {} ", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));
            List<DailyPlan> dailyPlan = extractDailyPlan(analysisJson.path("dailyPlan"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getActivityType() != null ? activity.getActivityType().toString() : "UNKNOWN")
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .dailyPlan(dailyPlan)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getActivityType() != null ? activity.getActivityType().toString() : "UNKNOWN")
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .dailyPlan(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<DailyPlan> extractDailyPlan(JsonNode dailyPlanNode) {
        List<DailyPlan> dailyPlan = new ArrayList<>();
        if (dailyPlanNode.isArray()) {
            dailyPlanNode.forEach(plan -> {
                DailyPlan dp = new DailyPlan();
                dp.setDay(plan.path("day").asText());
                dp.setActivity(plan.path("activity").asText());
                dp.setDurationMinutes(plan.path("durationMinutes").asInt(0));
                dp.setDescription(plan.path("description").asText());
                dailyPlan.add(dp);
            });
        }
        return dailyPlan;
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety guidelines") :
                safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ?
                Collections.singletonList("No specific suggestions provided") :
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));
            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No specific improvements provided") :
                improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity, String weatherContext, String userProfile) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ],
          "dailyPlan": [
            {
              "day": "Monday",
              "activity": "Activity name (e.g. Rest, Light Jog, Yoga)",
              "durationMinutes": 30,
              "description": "Why this activity is suitable for the weather and recovery"
            }
          ]
        }

        Athlete Profile:
        %s

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Weather Context:
        %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Adapt the recommendations and the 7-day calendar to the athlete's profile (height, weight, age) if available. For example, older athletes or those with high weight might need more recovery or lower intensity.
        Additionally, generate a 7-day 'dailyPlan' calendar starting from today. Adapt the recommended activities and their durations based on the weather context provided.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                userProfile,
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics(),
                weatherContext
        );
    }
}