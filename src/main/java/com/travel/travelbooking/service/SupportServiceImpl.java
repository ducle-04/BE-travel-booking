package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ConversationDTO;
import com.travel.travelbooking.dto.SendMessageRequest;
import com.travel.travelbooking.dto.SupportMessageDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.repository.ConversationRepository;
import com.travel.travelbooking.repository.SupportMessageRepository;
import com.travel.travelbooking.repository.UserRepository;
import com.travel.travelbooking.websocket.SupportWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupportServiceImpl implements SupportService {

    private final ConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SupportWebSocketController ws;

    @Autowired
    public SupportServiceImpl(ConversationRepository conversationRepository,
                              SupportMessageRepository messageRepository,
                              UserRepository userRepository,
                              SupportWebSocketController ws) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.ws = ws;
    }

    // ---------------------- GỬI TIN NHẮN ĐẦU TIÊN ----------------------

    @Override
    @Transactional
    public ConversationDTO sendInitialMessage(SendMessageRequest request, User currentUser) {
        Conversation conversation = new Conversation();
        conversation.setSubject(request.getSubject());

        boolean isStaff = currentUser != null &&
                currentUser.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (currentUser != null) {
            if (!isStaff) {
                conversation.setUser(currentUser);
            } else {
                conversation.setGuestName(request.getGuestName());
                conversation.setGuestEmail(request.getGuestEmail());
                conversation.setGuestPhone(request.getGuestPhone());
            }
        } else {
            conversation.setGuestName(request.getGuestName());
            conversation.setGuestEmail(request.getGuestEmail());
            conversation.setGuestPhone(request.getGuestPhone());
        }

        conversation = conversationRepository.save(conversation);

        SupportMessage message = new SupportMessage();
        message.setConversation(conversation);
        message.setContent(request.getContent());
        message.setSender(currentUser);
        message.setFromGuest(!isStaff);
        message.setRead(!isStaff ? false : true); // staff gửi → đã đọc
        message = messageRepository.save(message);

        // Bắn realtime vào phòng chat
        ws.sendMessageToConversation(conversation.getId(), toMessageDTO(message));

        // QUAN TRỌNG: Nếu là khách gửi → cập nhật danh sách cho staff
        if (message.isFromGuest()) {
            ConversationDTO convDto = toConversationDTO(conversation);
            ws.sendConversationListUpdate(convDto); // Gửi toàn bộ conversation → frontend tính unread
        }

        return toConversationDTO(conversation);
    }


    // REPLY TIN NHẮN

    @Override
    @Transactional
    public SupportMessageDTO replyMessage(Long conversationId, SendMessageRequest request, User currentUser) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        boolean isStaff = currentUser != null &&
                currentUser.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (!isStaff) {
            if (conversation.getUser() == null ||
                    !conversation.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Không có quyền reply conversation này");
            }
        }

        SupportMessage message = new SupportMessage();
        message.setConversation(conversation);
        message.setContent(request.getContent());
        message.setSender(currentUser);
        message.setFromGuest(!isStaff);
        message.setRead(!isStaff ? false : true); // staff gửi → đã đọc
        message = messageRepository.save(message);

        SupportMessageDTO dto = toMessageDTO(message);

        // Bắn realtime vào phòng chat
        ws.sendMessageToConversation(conversationId, dto);

        // QUAN TRỌNG: Nếu là khách gửi → cập nhật danh sách cho staff
        if (message.isFromGuest()) {
            ConversationDTO convDto = toConversationDTO(conversation);
            ws.sendConversationListUpdate(convDto); // Gửi toàn bộ conversation → frontend tính unread
        }

        return dto;
    }

    // ---------------------- LẤY TẤT CẢ TICKET (STAFF) ----------------------
    @Override
    public List<ConversationDTO> getAllConversations() {
        return conversationRepository.findAll().stream()
                .map(this::toConversationDTO)
                .collect(Collectors.toList());
    }

    // ---------------------- LẤY TICKET CỦA USER ----------------------
    @Override
    public List<ConversationDTO> getUserConversations(User user) {
        return conversationRepository.findByUser(user).stream()
                .map(this::toConversationDTO)
                .collect(Collectors.toList());
    }

    // ---------------------- STAFF LẤY CHI TIẾT TICKET ----------------------
    @Override
    public ConversationDTO getConversationById(Long id, User currentUser) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        boolean isStaff = currentUser != null &&
                currentUser.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (!isStaff) {
            throw new RuntimeException("Chỉ staff/admin được xem");
        }

        return toConversationDTO(conv);
    }

    // ---------------------- USER XEM TICKET CỦA CHÍNH MÌNH ----------------------
    @Override
    public ConversationDTO getConversationOfUser(Long id, User user) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        if (conv.getUser() == null ||
                !conv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền xem conversation này");
        }

        return toConversationDTO(conv);
    }

    // ---------------------- ĐÓNG TICKET ----------------------
    // ------------------------------------------------------------------------
    // ĐÓNG CONVERSATION (REALTIME)
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    public ConversationDTO closeConversation(Long id) {

        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        conv.setStatus(ConversationStatus.CLOSED);
        conv = conversationRepository.save(conv);

        ConversationDTO dto = toConversationDTO(conv);

        // 🔥 1) gửi cho Dashboard staff (list refresh)
        ws.sendConversationUpdate(dto);

        // 🔥 2) gửi cho user + admin đang mở room
        ws.sendConversationStatus(conv.getId(), dto);

        return dto;
    }


    // ---------------------- ĐÁNH DẤU ĐÃ ĐỌC ----------------------
    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        SupportMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message không tồn tại"));

        msg.setRead(true);
        messageRepository.save(msg);
        messageRepository.flush();
        ws.sendMessageToConversation(msg.getConversation().getId(), toMessageDTO(msg));
        ws.sendConversationListUpdate(toConversationDTO(msg.getConversation()));
    }

    // ---------------------- DTO CONVERT ----------------------
    private ConversationDTO toConversationDTO(Conversation conv) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setUserId(conv.getUser() != null ? conv.getUser().getId() : null);
        dto.setGuestName(conv.getGuestName());
        dto.setGuestEmail(conv.getGuestEmail());
        dto.setGuestPhone(conv.getGuestPhone());
        dto.setSubject(conv.getSubject());
        dto.setStatus(conv.getStatus());
        dto.setCreatedAt(conv.getCreatedAt());

        List<SupportMessage> messages =
                messageRepository.findByConversationOrderByCreatedAtAsc(conv);

        dto.setMessages(messages.stream().map(this::toMessageDTO).toList());


        return dto;
    }

    private SupportMessageDTO toMessageDTO(SupportMessage msg) {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setId(msg.getId());
        dto.setConversationId(msg.getConversation().getId());
        dto.setSenderId(msg.getSender() != null ? msg.getSender().getId() : null);
        dto.setSenderName(msg.getSender() != null ? msg.getSender().getFullname() : "Khách");
        dto.setContent(msg.getContent());
        dto.setCreatedAt(msg.getCreatedAt());
        dto.setFromGuest(msg.isFromGuest());
        dto.setRead(msg.isRead());
        return dto;
    }
}
