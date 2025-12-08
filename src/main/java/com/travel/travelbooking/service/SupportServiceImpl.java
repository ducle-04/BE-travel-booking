package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ConversationDTO;
import com.travel.travelbooking.dto.SendMessageRequest;
import com.travel.travelbooking.dto.SupportMessageDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.repository.ConversationRepository;
import com.travel.travelbooking.repository.SupportMessageRepository;
import com.travel.travelbooking.repository.UserRepository;
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

    @Autowired
    public SupportServiceImpl(ConversationRepository conversationRepository,
                              SupportMessageRepository messageRepository,
                              UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ConversationDTO sendInitialMessage(SendMessageRequest request, User currentUser) {
        Conversation conversation = new Conversation();
        conversation.setSubject(request.getSubject());

        if (currentUser != null) {
            conversation.setUser(currentUser);
        } else {
            if (request.getGuestName() == null || request.getGuestEmail() == null || request.getGuestPhone() == null) {
                throw new RuntimeException("Guest phải cung cấp tên, email, số điện thoại");
            }
            conversation.setGuestName(request.getGuestName());
            conversation.setGuestEmail(request.getGuestEmail());
            conversation.setGuestPhone(request.getGuestPhone());
        }

        conversation = conversationRepository.save(conversation);

        SupportMessage message = new SupportMessage();
        message.setConversation(conversation);
        message.setContent(request.getContent());
        message.setSender(currentUser);
        message.setFromGuest(currentUser == null);
        messageRepository.save(message);

        return toConversationDTO(conversation);
    }

    // ───────────────────────────────────────────────────────────
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
        message = messageRepository.save(message);

        return toMessageDTO(message);
    }

    // ───────────────────────────────────────────────────────────
    @Override
    public List<ConversationDTO> getAllConversations() {
        return conversationRepository.findAll().stream()
                .map(this::toConversationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationDTO> getUserConversations(User user) {
        return conversationRepository.findByUser(user).stream()
                .map(this::toConversationDTO)
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────────────────
    @Override
    public ConversationDTO getConversationById(Long id, User currentUser) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        boolean isStaff = currentUser != null &&
                currentUser.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (!isStaff) {
            throw new RuntimeException("Chỉ staff/admin được dùng API này");
        }

        return toConversationDTO(conv);
    }

    // ───────────────────────────────────────────────────────────
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

    // ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ConversationDTO closeConversation(Long id) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        conv.setStatus(ConversationStatus.CLOSED);
        return toConversationDTO(conversationRepository.save(conv));
    }

    // ───────────────────────────────────────────────────────────
    @Override
    public void markAsRead(Long messageId) {
        SupportMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message không tồn tại"));

        msg.setRead(true);
        messageRepository.save(msg);
    }

    // ───────────────────────────────────────────────────────────
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

        List<SupportMessage> messages = messageRepository.findByConversation(conv);
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
