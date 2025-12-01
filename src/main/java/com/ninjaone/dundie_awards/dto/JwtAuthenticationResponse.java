package com.ninjaone.dundie_awards.dto;

public record JwtAuthenticationResponse(
        String token,
        String type,
        String username
) {
    public JwtAuthenticationResponse(String token, String username) {
        this(token, "Bearer", username);
    }
}

