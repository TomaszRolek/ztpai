package com.example.ztpai.Message;

import com.example.ztpai.Chat.ChatResponse;

public record MessageWebsocketResponse(
        MessageResponse message,
        ChatResponse chatInContextOfSender,
        ChatResponse chatInContextOfRecipient)
{}
