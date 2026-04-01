package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Model.UserPrinciple;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private BCryptPasswordEncoder encoder =new BCryptPasswordEncoder(12);

    public RequestRespondUser register(RequestRespondUser userRegistrationRequest) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Users user = new Users();
            user.setUsername(userRegistrationRequest.getUsername());
            user.setPassword(encoder.encode(userRegistrationRequest.getPassword()));
            user.setRole(userRegistrationRequest.getRole());
            user.setEmail(userRegistrationRequest.getEmail());
            Users userRegistered = userRepo.save(user);
            if(userRegistered.getId() > 0) {
                requestRespondUser.setUser(userRegistered);
                requestRespondUser.setStatusCode(200);
                requestRespondUser.setMessage("User has been registered successfully");
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage(e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser login(RequestRespondUser userLoginRequest) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword()));
            if(authentication.isAuthenticated()) {
                Optional<Users> optionalUsers = userRepo.findByUsername(userLoginRequest.getUsername());
                if(optionalUsers.isPresent()) {
                    var user = optionalUsers.get();
                    UserPrinciple userPrinciple = new UserPrinciple(user);
                    var jwtToken = jwtService.generateToken(user.getUsername());
                    var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userPrinciple);
                    requestRespondUser.setStatusCode(200);
                    requestRespondUser.setToken(jwtToken);
                    requestRespondUser.setUsername(user.getUsername());
                    requestRespondUser.setRole(user.getRole());
                    requestRespondUser.setEmail(user.getEmail());
                    requestRespondUser.setRefreshToken(refreshToken);
                    requestRespondUser.setUser(user);
                    requestRespondUser.setExpirationTime("24Hrs");
                    requestRespondUser.setMessage("Successfully Logged In");
                } else {
                    requestRespondUser.setStatusCode(404);
                    requestRespondUser.setMessage("User not found");
                }
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage(e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser resetPassword(RequestRespondUser requestRespondUserResetPassword) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try{
            Optional<Users> optionalUsers = userRepo.findByUsername(requestRespondUserResetPassword.getUsername());
            if(optionalUsers.isPresent()) {
                Users userToResetPassword = optionalUsers.get();
                if(Objects.nonNull(requestRespondUserResetPassword.getPassword()) && !requestRespondUserResetPassword.getPassword().isEmpty()) {
                    userToResetPassword.setPassword(encoder.encode(requestRespondUserResetPassword.getPassword()));
                }
                Users savedUser = userRepo.save(userToResetPassword);
                requestRespondUser.setUser(savedUser);
                requestRespondUser.setStatusCode(200);
                requestRespondUser.setMessage("User's password reset successfully");
            } else {
                requestRespondUser.setStatusCode(404);
                requestRespondUser.setMessage("User not found for reset-password");
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred when resetting user's password :" + e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser getUserProfile(String requestUsername) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Optional<Users> optionalUsers = userRepo.findByUsername(requestUsername);
            if (optionalUsers.isPresent()) {
                Users users = optionalUsers.get();
                requestRespondUser.setUser(users);
                requestRespondUser.setStatusCode(200);
                requestRespondUser.setMessage("User with username : " + requestUsername + " found successfully");
            } else {
                requestRespondUser.setStatusCode(404);
                requestRespondUser.setMessage("Username not found");
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred when getting user by id : " + e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser getUserById(Integer requestUserById) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Users userById = userRepo.findById(requestUserById).orElseThrow(()-> new RuntimeException("User not found"));
            requestRespondUser.setUser(userById);
            requestRespondUser.setStatusCode(200);
            requestRespondUser.setMessage("User with id : " + requestUserById + " found successfully");
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred when getting user by id : " + e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser getAllUserInfo() {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            List<Users> listOfUsers = userRepo.findAll();
            requestRespondUser.setListOfUsers(listOfUsers);
            requestRespondUser.setStatusCode(200);
            requestRespondUser.setMessage("List of users found successfully");
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred when getting user by id : " + e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser updateUser(String oldUsername, Users requestUpdateUser) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Optional<Users> optionalUser = userRepo.findByUsername(oldUsername);
            if(optionalUser.isPresent()) {
                Users user = getUser(requestUpdateUser, optionalUser);
                Users savedUser = userRepo.save(user);
                requestRespondUser.setUsername(savedUser.getUsername());
                requestRespondUser.setUser(savedUser);
                requestRespondUser.setStatusCode(200);
                requestRespondUser.setMessage("User updated successfully");
            } else {
                requestRespondUser.setStatusCode(404);
                requestRespondUser.setMessage("User not found for update");
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred when updating user :" + e.getMessage());
        }
        return requestRespondUser;
    }

    public RequestRespondUser deleteUser(Integer requestUserById) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        try {
            Optional<Users> usersOptional = userRepo.findById(requestUserById);
            if(usersOptional.isPresent()) {
                userRepo.deleteById(requestUserById);
                requestRespondUser.setStatusCode(200);
                requestRespondUser.setMessage("User successfully deleted");
            } else {
                requestRespondUser.setStatusCode(404);
                requestRespondUser.setMessage("User not found for deletion");
            }
        } catch (Exception e) {
            requestRespondUser.setStatusCode(500);
            requestRespondUser.setMessage("Error occurred while deleting user :" + e.getMessage());
        }
        return requestRespondUser;
    }

    private Users getUser(Users requestUpdateUser, Optional<Users> optionalUsers) {
        Users user = optionalUsers.get();
        if(Objects.nonNull(requestUpdateUser.getUsername()) && !requestUpdateUser.getUsername().isEmpty()) {
            user.setUsername(requestUpdateUser.getUsername());
        }
        if(Objects.nonNull(requestUpdateUser.getPassword()) && !requestUpdateUser.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(requestUpdateUser.getPassword()));
        }
        if(Objects.nonNull(requestUpdateUser.getEmail()) && !requestUpdateUser.getEmail().isEmpty()) {
            user.setEmail(requestUpdateUser.getEmail());
        }
        return user;
    }
}
