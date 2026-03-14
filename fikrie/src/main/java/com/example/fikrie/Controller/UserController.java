package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-user-info")
    public ResponseEntity<RequestRespondUser> getUserInfo(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.getUserProfile(requestRespondUser.getUsers().getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<RequestRespondUser> register(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.register(requestRespondUser));
    }

    @PostMapping("/login")
    public ResponseEntity<RequestRespondUser> login(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.login(requestRespondUser));
    }

    @PutMapping("reset-password")
    public ResponseEntity<RequestRespondUser> resetPassword(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.resetPassword(requestRespondUser));
    }

    @PutMapping("/update-user")
    public ResponseEntity<RequestRespondUser> updateUser(@RequestBody RequestRespondUser requestUpdateUser) {
        return ResponseEntity.ok(userService.updateUser(requestUpdateUser.getUsername(), requestUpdateUser.getUsers()));
    }

}
