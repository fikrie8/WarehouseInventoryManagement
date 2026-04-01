package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get-user-info/{requestUsername}")
    public ResponseEntity<RequestRespondUser> getUserInfo(@PathVariable(required = true) String requestUsername) {
        return ResponseEntity.ok(userService.getUserProfile(requestUsername));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<RequestRespondUser> resetPassword(@RequestBody RequestRespondUser requestRespondUser) {
        return ResponseEntity.ok(userService.resetPassword(requestRespondUser));
    }

    @PutMapping("/update-user")
    public ResponseEntity<RequestRespondUser> updateUser(@RequestBody RequestRespondUser requestUpdateUser) {
        return ResponseEntity.ok(userService.updateUser(requestUpdateUser.getUsername(), requestUpdateUser.getUser()));
    }

}
