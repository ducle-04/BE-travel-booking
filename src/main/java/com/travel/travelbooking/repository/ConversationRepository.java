package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Conversation;
import com.travel.travelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);
    Optional<Conversation> findByIdAndUser(Long id, User user);  // Để user xem conversation của mình
    List<Conversation> findByGuestEmail(String guestEmail);  // Để guest xem nếu cung cấp email (nhưng basic, chưa implement UI)
}