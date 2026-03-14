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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-user-info/{requestUserId}")
    public ResponseEntity<RequestRespondUser> getUserInfoById(@PathVariable(required = true) Integer requestUserId) {
        return ResponseEntity.ok(userService.getUserById(requestUserId));
    }

    @PutMapping("/update-user")
    public ResponseEntity<RequestRespondUser> updateUser(@RequestBody RequestRespondUser requestUpdateUser) {
        return ResponseEntity.ok(userService.updateUser(requestUpdateUser.getUsername(), requestUpdateUser.getUsers()));
    }

    @DeleteMapping("/delete-user/{requestDeleteId}")
    public ResponseEntity<RequestRespondUser> deleteUser(@PathVariable(required = true) Integer requestDeleteId) {
        return ResponseEntity.ok(userService.deleteUser(requestDeleteId));
    }

}
