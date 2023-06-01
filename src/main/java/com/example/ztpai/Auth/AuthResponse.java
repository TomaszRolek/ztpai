package com.example.ztpai.Auth;

import java.time.Instant;

public record AuthResponse (
        String accessToken,
        Instant expiresAt,
        String refreshToken,
        String username
){}
