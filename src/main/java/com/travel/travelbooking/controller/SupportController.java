package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.ConversationDTO;
import com.travel.travelbooking.dto.SendMessageRequest;
import com.travel.travelbooking.dto.SupportMessageDTO;
import com.travel.travelbooking.entity.User;
import com.travel.travelbooking.service.SupportService;
import com.travel.travelbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;
    private final UserService userService;

    public SupportController(SupportService supportService, UserService userService) {
        this.supportService = supportService;
        this.userService = userService;
    }

    // ───────────────────────────────────────────────────────────
    @PostMapping("/contact")
    public ResponseEntity<ConversationDTO> sendContact(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        if (currentUser != null) {
            request.setGuestName(currentUser.getFullname());
            request.setGuestEmail(currentUser.getEmail());
            request.setGuestPhone(currentUser.getPhoneNumber());
        }

        return ResponseEntity.ok(supportService.sendInitialMessage(request, currentUser));
    }

    // ───────────────────────────────────────────────────────────
    @PostMapping("/reply/{conversationId}")
    public ResponseEntity<SupportMessageDTO> replyMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        return ResponseEntity.ok(supportService.replyMessage(conversationId, request, currentUser));
    }

    // ───────────────────────────────────────────────────────────
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        return ResponseEntity.ok(supportService.getAllConversations());
    }

    // ───────────────────────────────────────────────────────────
    @GetMapping("/my-conversations")
    public ResponseEntity<List<ConversationDTO>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(supportService.getUserConversations(user));
    }

    // ───────────────────────────────────────────────────────────
    @GetMapping("/my-conversation/{id}")
    public ResponseEntity<ConversationDTO> getMyConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(supportService.getConversationOfUser(id, user));
    }

    // ───────────────────────────────────────────────────────────
    @GetMapping("/conversation/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ConversationDTO> getConversationForStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User staff = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(supportService.getConversationById(id, staff));
    }

    // ───────────────────────────────────────────────────────────
    @PutMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ConversationDTO> closeConversation(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.closeConversation(id));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<?> markMessageRead(@PathVariable Long id) {
        supportService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

}
