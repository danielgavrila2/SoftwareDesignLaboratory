package com.fitness.activityservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "strava_profiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StravaProfile {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    private String clientId;
    private String clientSecret;
    
    private String accessToken;
    private String refreshToken;
    private Long expiresAt; // Epoch seconds
}
