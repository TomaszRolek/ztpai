package com.example.ztpai.Message;

import com.example.ztpai.Chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChat_ChatIdOrderByCreated(Long chatId);
    void deleteAllByChat_ChatId(Long chatId);
    Optional<Message> findTopByChatOrderByCreatedDesc(Chat chat);
    int countByChat(Chat chat);
}
