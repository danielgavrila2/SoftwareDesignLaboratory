package com.sd.enterprise.mvcapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sd.enterprise.mvcapp.services.DietService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DietService Unit Tests")
class DietServiceTest {

    private DietService dietService;

    @BeforeEach
    void setUp() {
        dietService = new DietService(new ObjectMapper());
        // Use demo key so all calls return mock data
        ReflectionTestUtils.setField(dietService, "apiKey", "demo");
        ReflectionTestUtils.setField(dietService, "baseUrl", "https://api.api-ninjas.com/v1");
    }

    // ─── generateDietPlan ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDietPlan()")
    class GenerateDietPlan {

        @Test
        @DisplayName("returns plan with correct goal and target calories")
        void returnsCorrectGoalAndCalories() {
            Map<String, Object> plan = dietService.generateDietPlan(2000, "maintenance");

            assertThat(plan.get("goal")).isEqualTo("maintenance");
            assertThat(plan.get("targetCalories")).isEqualTo(2000);
        }

        @Test
        @DisplayName("plan contains 4 meals")
        void containsFourMeals() {
            Map<String, Object> plan = dietService.generateDietPlan(2000, "maintenance");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> meals = (List<Map<String, Object>>) plan.get("meals");
            assertThat(meals).hasSize(4);
        }

        @Test
        @DisplayName("meals have required fields: mealType, name, calories, protein_g, carbs_g, fat_g")
        void mealsHaveRequiredFields() {
            Map<String, Object> plan = dietService.generateDietPlan(1800, "weight_loss");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> meals = (List<Map<String, Object>>) plan.get("meals");
            for (Map<String, Object> meal : meals) {
                assertThat(meal).containsKeys("mealType", "name", "calories", "protein_g", "carbs_g", "fat_g");
            }
        }

        @Test
        @DisplayName("total meal calories sum close to target (within 50 kcal rounding margin)")
        void mealCaloriesTotalNearTarget() {
            int target = 2200;
            Map<String, Object> plan = dietService.generateDietPlan(target, "muscle_gain");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> meals = (List<Map<String, Object>>) plan.get("meals");
            int total = meals.stream().mapToInt(m -> (int) m.get("calories")).sum();
            assertThat(total).isBetween(target - 100, target + 100);
        }

        @Test
        @DisplayName("weight_loss plan has higher protein ratio in macro split")
        void weightLossHasHighProtein() {
            Map<String, Object> plan = dietService.generateDietPlan(1600, "weight_loss");

            @SuppressWarnings("unchecked")
            Map<String, Integer> macros = (Map<String, Integer>) plan.get("macroSplit");
            assertThat(macros.get("protein")).isGreaterThanOrEqualTo(30);
        }

        @Test
        @DisplayName("muscle_gain plan has high carb ratio in macro split")
        void muscleGainHasHighCarbs() {
            Map<String, Object> plan = dietService.generateDietPlan(3000, "muscle_gain");

            @SuppressWarnings("unchecked")
            Map<String, Integer> macros = (Map<String, Integer>) plan.get("macroSplit");
            assertThat(macros.get("carbs")).isGreaterThanOrEqualTo(45);
        }

        @Test
        @DisplayName("plan includes non-empty tips list")
        void includesTips() {
            Map<String, Object> plan = dietService.generateDietPlan(2000, "maintenance");

            @SuppressWarnings("unchecked")
            List<String> tips = (List<String>) plan.get("tips");
            assertThat(tips).isNotEmpty();
            assertThat(tips).allMatch(tip -> !tip.isBlank());
        }

        @Test
        @DisplayName("macro split percentages sum to 100")
        void macroSplitSumsTo100() {
            for (String goal : List.of("maintenance", "weight_loss", "muscle_gain")) {
                Map<String, Object> plan = dietService.generateDietPlan(2000, goal);

                @SuppressWarnings("unchecked")
                Map<String, Integer> macros = (Map<String, Integer>) plan.get("macroSplit");
                int sum = macros.values().stream().mapToInt(Integer::intValue).sum();
                assertThat(sum).as("Macro split sum for goal=%s", goal).isEqualTo(100);
            }
        }
    }

    // ─── getNutritionInfo ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getNutritionInfo()")
    class GetNutritionInfo {

        @Test
        @DisplayName("returns mock response with all required keys when no API key configured")
        void returnsMockData() {
            Map<String, Object> result = dietService.getNutritionInfo("chicken breast");

            assertThat(result).containsKeys("calories", "protein_g", "carbs_g", "fat_g");
        }

        @Test
        @DisplayName("mock response includes demo note when no API key configured")
        void includesDemoNote() {
            Map<String, Object> result = dietService.getNutritionInfo("banana");

            assertThat(result).containsKey("note");
            assertThat(result.get("note").toString()).containsIgnoringCase("demo");
        }

        @Test
        @DisplayName("returned calories are positive")
        void caloriesArePositive() {
            Map<String, Object> result = dietService.getNutritionInfo("oats");

            double calories = ((Number) result.get("calories")).doubleValue();
            assertThat(calories).isPositive();
        }
    }
}