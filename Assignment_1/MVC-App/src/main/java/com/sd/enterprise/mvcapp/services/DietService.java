package com.sd.enterprise.mvcapp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {

    private final ObjectMapper objectMapper;

    @Value("${fitcore.api-ninjas.key:demo}")
    private String apiKey;

    @Value("${fitcore.api-ninjas.base-url:https://api.api-ninjas.com/v1}")
    private String baseUrl;

    public Map<String, Object> generateDietPlan(int calorieGoal, String goal) {
        List<Map<String, Object>> meals = new ArrayList<>();

        if ("demo".equals(apiKey) || apiKey.contains("YOUR_API")) {
            // Return mock diet plan when no API key configured
            meals = getMockMeals(calorieGoal);
        } else {
            meals = fetchNutritionData(calorieGoal);
        }

        int totalCalories = meals.stream()
                .mapToInt(m -> (int) m.getOrDefault("calories", 0))
                .sum();

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("goal", goal);
        plan.put("targetCalories", calorieGoal);
        plan.put("actualCalories", totalCalories);
        plan.put("meals", meals);
        plan.put("tips", getDietTips(goal));
        plan.put("macroSplit", getMacroSplit(goal));

        return plan;
    }

    public Map<String, Object> getNutritionInfo(String query) {
        if ("demo".equals(apiKey) || apiKey.contains("YOUR_API")) {
            return getMockNutrition(query);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Api-Key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/nutrition?query=" + query.replace(" ", "%20");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            Map<String, Object> result = new LinkedHashMap<>();

            if (root.isArray() && !root.isEmpty()) {
                JsonNode item = root.get(0);
                result.put("name", item.path("name").asText());
                result.put("calories", item.path("calories").asDouble());
                result.put("protein_g", item.path("protein_g").asDouble());
                result.put("carbs_g", item.path("carbohydrates_total_g").asDouble());
                result.put("fat_g", item.path("fat_total_g").asDouble());
                result.put("fiber_g", item.path("fiber_g").asDouble());
            }
            return result;
        } catch (Exception e) {
            log.warn("Nutrition API call failed, returning mock data: {}", e.getMessage());
            return getMockNutrition(query);
        }
    }

    private List<Map<String, Object>> getMockMeals(int calorieGoal) {
        int breakfastCal = (int) (calorieGoal * 0.25);
        int lunchCal = (int) (calorieGoal * 0.35);
        int dinnerCal = (int) (calorieGoal * 0.30);
        int snackCal = calorieGoal - breakfastCal - lunchCal - dinnerCal;

        return List.of(
                createMeal("Breakfast", "Oatmeal with berries and protein shake",
                        breakfastCal, 30, 55, 8),
                createMeal("Lunch", "Grilled chicken breast with quinoa and vegetables",
                        lunchCal, 40, 40, 12),
                createMeal("Snack", "Greek yogurt with almonds",
                        snackCal, 15, 20, 10),
                createMeal("Dinner", "Salmon with sweet potato and broccoli",
                        dinnerCal, 38, 45, 12)
        );
    }

    private Map<String, Object> createMeal(String mealType, String name, int calories,
                                           int proteinG, int carbsG, int fatG) {
        Map<String, Object> meal = new LinkedHashMap<>();
        meal.put("mealType", mealType);
        meal.put("name", name);
        meal.put("calories", calories);
        meal.put("protein_g", proteinG);
        meal.put("carbs_g", carbsG);
        meal.put("fat_g", fatG);
        return meal;
    }

    private List<Map<String, Object>> fetchNutritionData(int calorieGoal) {
        // Simplified fetch - in real world you'd fetch multiple foods
        return getMockMeals(calorieGoal);
    }

    private Map<String, Object> getMockNutrition(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", query);
        result.put("calories", 250.0);
        result.put("protein_g", 20.0);
        result.put("carbs_g", 30.0);
        result.put("fat_g", 8.0);
        result.put("fiber_g", 4.0);
        result.put("note", "Demo data - add API Ninjas key for real nutrition info");
        return result;
    }

    private List<String> getDietTips(String goal) {
        return switch (goal.toLowerCase()) {
            case "weight_loss" -> List.of(
                    "Create a 500 calorie deficit per day",
                    "Prioritize protein to preserve muscle mass",
                    "Eat plenty of fiber-rich vegetables",
                    "Stay hydrated - drink 2-3L of water daily"
            );
            case "muscle_gain" -> List.of(
                    "Eat in a 300-500 calorie surplus",
                    "Target 1.6-2.2g protein per kg bodyweight",
                    "Time carbohydrates around workouts",
                    "Don't skip meals - consistency is key"
            );
            default -> List.of(
                    "Eat balanced meals with protein, carbs, and healthy fats",
                    "Limit processed foods and added sugars",
                    "Meal prep to stay on track",
                    "Listen to your hunger cues"
            );
        };
    }

    private Map<String, Integer> getMacroSplit(String goal) {
        return switch (goal.toLowerCase()) {
            case "weight_loss" -> Map.of("protein", 35, "carbs", 35, "fat", 30);
            case "muscle_gain" -> Map.of("protein", 30, "carbs", 50, "fat", 20);
            default -> Map.of("protein", 25, "carbs", 50, "fat", 25);
        };
    }
}
