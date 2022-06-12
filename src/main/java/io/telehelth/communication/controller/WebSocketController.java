package io.telehelth.communication.controller;


import io.telehelth.communication.service.OnlineUsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin
public class WebSocketController {
    private final OnlineUsersService usersService;

    public WebSocketController(OnlineUsersService usersService) {
        this.usersService = usersService;
    }

    @RequestMapping("/vdt-status")
    @ResponseBody
    public ResponseEntity<Map<String,Integer>> getStatus(){
        return ResponseEntity.ok(usersService.getStatus());
    }

}
