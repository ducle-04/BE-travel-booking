package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Conversation;
import com.travel.travelbooking.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByConversation(Conversation conversation);
}