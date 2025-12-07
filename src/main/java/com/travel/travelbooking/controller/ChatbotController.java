package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.ChatHistoryDTO;
import com.travel.travelbooking.entity.ChatHistory;
import com.travel.travelbooking.service.GroqChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final GroqChatService chatService;


    // hỏi không cần đăng nhập
    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            @RequestBody ChatRequest req,
            Principal principal // có thể null
    ) {
        String username = (principal != null) ? principal.getName() : null;

        String reply = chatService.chat(req.getMessage(), username);

        return ResponseEntity.ok(new ChatResponse(reply));
    }


    // xem lịch sử -> cần đăng nhập
    @GetMapping("/history")
    public ResponseEntity<?> history(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để xem lịch sử.");
        }

        List<ChatHistoryDTO> list = chatService.getHistory(principal.getName());
        return ResponseEntity.ok(list); // giờ an toàn 100%
    }
}


@Data
class ChatRequest {
    private String message;
}

@Data
class ChatResponse {
    private final String reply;
}
