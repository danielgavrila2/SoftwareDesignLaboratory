package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Default to London since we don't have user coordinates stored yet
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast?latitude=51.5074&longitude=-0.1278&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto";

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getWeeklyForecastContext() {
        try {
            String response = restTemplate.getForObject(WEATHER_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode daily = root.path("daily");
            
            if (daily.isMissingNode()) {
                return "Weather forecast unavailable.";
            }

            JsonNode time = daily.path("time");
            JsonNode tempMax = daily.path("temperature_2m_max");
            JsonNode tempMin = daily.path("temperature_2m_min");
            JsonNode precip = daily.path("precipitation_sum");

            StringBuilder forecastBuilder = new StringBuilder();
            forecastBuilder.append("Upcoming 7-Day Weather Forecast (London):\n");
            
            for (int i = 0; i < time.size() && i < 7; i++) {
                String date = time.get(i).asText();
                double maxTemp = tempMax.get(i).asDouble();
                double minTemp = tempMin.get(i).asDouble();
                double rain = precip.get(i).asDouble();

                forecastBuilder.append(String.format("- %s: Max %.1f°C, Min %.1f°C, Rain %.1fmm\n", 
                        date, maxTemp, minTemp, rain));
            }
            
            return forecastBuilder.toString();
        } catch (Exception e) {
            log.error("Failed to fetch weather forecast: {}", e.getMessage());
            return "Weather forecast unavailable due to an error.";
        }
    }
}
