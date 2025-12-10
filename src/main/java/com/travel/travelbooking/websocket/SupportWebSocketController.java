package com.travel.travelbooking.websocket;

import com.travel.travelbooking.dto.ConversationDTO;
import com.travel.travelbooking.dto.SupportMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SupportWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SupportWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gửi message mới tới user và staff subscribe conversation này
     */
    public void sendMessageToConversation(Long conversationId, SupportMessageDTO message) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
    }

    /**
     * Gửi cập nhật conversation cho Dashboard Staff
     */
    public void sendConversationUpdate(ConversationDTO dto) {
        messagingTemplate.convertAndSend("/topic/support", dto);
    }

    /**
     * 🔥 Gửi cập nhật trạng thái (VD: CLOSED) tới room conversation
     */
    public void sendConversationStatus(Long conversationId, ConversationDTO dto) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, dto);
    }

    public void sendConversationListUpdate(ConversationDTO dto) {
        messagingTemplate.convertAndSend("/topic/support", dto);
    }

}
