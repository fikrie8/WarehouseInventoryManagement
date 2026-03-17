package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class UserRegisterLoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RequestRespondUser> register(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.register(requestRespondUser));
    }

    @PostMapping("/login")
    public ResponseEntity<RequestRespondUser> login(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.login(requestRespondUser));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<RequestRespondUser> resetPassword(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.resetPassword(requestRespondUser));
    }

}
