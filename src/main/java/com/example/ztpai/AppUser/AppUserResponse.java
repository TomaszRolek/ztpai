package com.example.ztpai.AppUser;

public record AppUserResponse (
        Long appUserId,
        String username,
        String avatar,
        String created
){}
