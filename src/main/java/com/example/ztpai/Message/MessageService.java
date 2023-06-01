package com.example.ztpai.Message;

import com.example.ztpai.AppUser.AppUser;
import com.example.ztpai.AppUser.AppUserRepository;
import com.example.ztpai.Auth.AuthService;
import com.example.ztpai.Chat.Chat;
import com.example.ztpai.Chat.ChatRepository;
import com.example.ztpai.Chat.ChatService;
import com.example.ztpai.Utils.Pair;
import com.example.ztpai.Utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final ChatService chatService;

    @Autowired
    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository, AppUserRepository appUserRepository, ChatService chatService, AuthService authService) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.appUserRepository = appUserRepository;
        this.authService = authService;
        this.chatService = chatService;
    }

    @Transactional
    public Pair<MessageWebsocketResponse, Boolean> sendMessage(String recipientName, String text) {
        AppUser sender = authService.getUserFromAuthentication();

        Optional<AppUser> recipientOptional = appUserRepository.findByUsername(recipientName);
        if (recipientOptional.isEmpty())
            throw new RuntimeException("Recipient " + recipientName + " not found");
        AppUser recipient = recipientOptional.get();

        if (sender.equals(recipient))
            throw new RuntimeException("You cannot message yourself");

        Optional<Chat> chatOptional = chatRepository.findByCreatorAndMember(sender, recipient)
                .or(() -> chatRepository.findByCreatorAndMember(recipient, sender));
        Chat chat;
        boolean newChatCreated = false;
        if(chatOptional.isEmpty()) {
            chat = Chat.builder()
                    .creator(sender)
                    .member(recipient)
                    .created(Instant.now())
                    .modified(Instant.now())
                    .build();
            chatRepository.save(chat);
            newChatCreated = true;
        } else {
            chat = chatOptional.get();
        }

        chat.setModified(Instant.now());
        chatRepository.save(chat);

        Message message = Message.builder()
                .text(text)
                .chat(chat)
                .sender(sender)
                .created(Instant.now())
                .build();
        messageRepository.save(message);

        return new Pair<>(mapMessageToWebsocketResponse(message, chat, sender, recipient), newChatCreated);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByChat(Long chatId){
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if (chatOptional.isEmpty())
            throw new RuntimeException("Chat of id " + chatId + " not found");

        return messageRepository
                .findAllByChat_ChatIdOrderByCreated(chatId)
                .stream()
                .map(this::mapMessageToResponse)
                .toList();
    }

    private MessageResponse mapMessageToResponse(Message message) {
        return new MessageResponse(
                message.getMessageId(),
                message.getText(),
                message.getSender().getUsername(),
                Time.formatTime(message.getCreated())
        );
    }

    private MessageWebsocketResponse mapMessageToWebsocketResponse(Message message, Chat chat, AppUser sender, AppUser recipient) {
        return new MessageWebsocketResponse(
                mapMessageToResponse(message),
                chatService.mapChatToResponse(chat, sender),
                chatService.mapChatToResponse(chat, recipient)
        );
    }
}
