package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.ChatHistory;
import com.travel.travelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserIdOrderByTimestampAsc(Long userId);
    List<ChatHistory> findTop5ByUserOrderByTimestampDesc(User user);
}
