package com.example.ztpai.Auth;

public record RefreshTokenRequest (
        String username,
        String refreshToken
){}
