package com.example.ztpai.Message;

public record MessageResponse(
        Long messageId,
        String text,
        String senderName,
        String timeSent)
{}
