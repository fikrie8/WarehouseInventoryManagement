package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;

    @GetMapping("/get-user-info/{requestUserId}")
    @PreAuthorize("#requestUsername == authentication.principal.username or hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<RequestRespondUser> getUserInfoById(@PathVariable(required = true) Integer requestUserId) {
        return ResponseEntity.ok(userService.getUserById(requestUserId));
    }

    @GetMapping("/get-user-info/all")
    public ResponseEntity<RequestRespondUser> getAllUserInfo() {
        return ResponseEntity.ok(userService.getAllUserInfo());
    }

    @PutMapping("/update-user")
    public ResponseEntity<RequestRespondUser> updateUser(@RequestBody RequestRespondUser requestUpdateUser) {
        return ResponseEntity.ok(userService.updateUser(requestUpdateUser.getUsername(), requestUpdateUser.getUser()));
    }

    @PutMapping("/update-user/{requestUserId}")
    public ResponseEntity<RequestRespondUser> updateUserById(@PathVariable(required = true) Integer requestUserId, @RequestBody RequestRespondUser requestUpdateUser) {
        return ResponseEntity.ok(userService.updateUserById(requestUserId, requestUpdateUser.getUser()));
    }
}
