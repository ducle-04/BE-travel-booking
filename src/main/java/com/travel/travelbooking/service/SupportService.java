package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ConversationDTO;
import com.travel.travelbooking.dto.SendMessageRequest;
import com.travel.travelbooking.dto.SupportMessageDTO;
import com.travel.travelbooking.entity.User;

import java.util.List;

public interface SupportService {

    ConversationDTO sendInitialMessage(SendMessageRequest request, User currentUser);

    SupportMessageDTO replyMessage(Long conversationId, SendMessageRequest request, User currentUser);

    List<ConversationDTO> getAllConversations();

    List<ConversationDTO> getUserConversations(User user);

    // Staff/Admin + User đều có thể gọi qua controller khác
    ConversationDTO getConversationById(Long id, User currentUser);

    // User chỉ xem conversation của họ
    ConversationDTO getConversationOfUser(Long id, User currentUser);

    ConversationDTO closeConversation(Long id);

    void markAsRead(Long messageId);
}
