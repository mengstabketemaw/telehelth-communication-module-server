package io.telehelth.communication.controller;


import io.telehelth.communication.model.ChatMessage;
import io.telehelth.communication.service.OnlineUsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin
public class WebSocketController {
    private final OnlineUsersService usersService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(OnlineUsersService usersService, SimpMessagingTemplate messagingTemplate) {
        this.usersService = usersService;
        this.messagingTemplate = messagingTemplate;
    }

    @RequestMapping("/vdt-status")
    @ResponseBody
    public ResponseEntity<Map<String,Integer>> getStatus(){
        return ResponseEntity.ok(usersService.getStatus());
    }

    @MessageMapping("/chat/doctor")
    @SendTo("/topic/chat/doctor")
    public ChatMessage groupChatPatient(ChatMessage message){
        return message;
    }

    @MessageMapping("/chat/patient")
    @SendTo("/topic/chat/patient")
    public ChatMessage groupChatDoctor(ChatMessage message){
        return message;
    }
}
