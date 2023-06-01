package com.example.ztpai.Chat;

public record ChatResponse(
        Long chatId,
        String otherMemberUsername,
        String otherMemberAvatar,
        String lastMessage,
        String lastMessageTime,
        int messageCount)
{}
