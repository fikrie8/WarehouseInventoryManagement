package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Model.UserPrinciple;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public RequestRespondUser register(RequestRespondUser userRegistrationRequest) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        Users user = new Users();
        user.setUsername(userRegistrationRequest.getUsername());
        user.setPassword(encoder.encode(userRegistrationRequest.getPassword()));
        user.setRole(userRegistrationRequest.getRole());
        user.setEmail(userRegistrationRequest.getEmail());
        Users userRegistered = userRepo.save(user);

        requestRespondUser.setUser(userRegistered);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User has been registered successfully");

        return requestRespondUser;
    }

    public RequestRespondUser login(RequestRespondUser userLoginRequest) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword()));
        Users user = userRepo.findByUsername(userLoginRequest.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException("User record not found in system : " + userLoginRequest.getUsername()));
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

        return requestRespondUser;
    }

    public RequestRespondUser resetPassword(RequestRespondUser requestRespondUserResetPassword) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        Users userToResetPassword = userRepo.findByUsername(requestRespondUserResetPassword.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found in system for reset-password : " + requestRespondUser.getUsername()));
        if(Objects.nonNull(requestRespondUserResetPassword.getPassword()) && !requestRespondUserResetPassword.getPassword().isEmpty()) {
            userToResetPassword.setPassword(encoder.encode(requestRespondUserResetPassword.getPassword()));
        }
        Users user = userRepo.save(userToResetPassword);

        requestRespondUser.setUser(user);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User's password reset successfully");

        return requestRespondUser;
    }

    public RequestRespondUser getUserProfile(String requestUsername) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        Users users = userRepo.findByUsername(requestUsername)
                            .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + requestUsername));

        requestRespondUser.setUser(users);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User with username : " + requestUsername + " found successfully");

        return requestRespondUser;
    }

    public RequestRespondUser getUserById(Integer requestUserById) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        Users userById = userRepo.findById(requestUserById)
                                .orElseThrow(()-> new UsernameNotFoundException("User not found with ID : " + requestUserById));

        requestRespondUser.setUser(userById);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User with id : " + requestUserById + " found successfully");

        return requestRespondUser;
    }

    public RequestRespondUser getAllUserInfo() {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        List<Users> listOfUsers = userRepo.findAll();

        requestRespondUser.setListOfUsers(listOfUsers);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("List of users found successfully");

        return requestRespondUser;
    }

    public RequestRespondUser updateUser(String username, Users requestUpdateUser) {
        RequestRespondUser requestRespondUser = new RequestRespondUser();
        Users oldUser = userRepo.findByUsername(username)
                            .orElseThrow(()-> new UsernameNotFoundException("User not found for update : " + username));

        Users newUpdatedUser = checkUserNewInformation(requestUpdateUser, oldUser);
        Users savedUser = userRepo.save(newUpdatedUser);
        requestRespondUser.setUsername(savedUser.getUsername());
        requestRespondUser.setFirstName(savedUser.getFirstName());
        requestRespondUser.setLastName(savedUser.getLastName());
        requestRespondUser.setEmail(savedUser.getEmail());
        requestRespondUser.setUser(savedUser);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User updated successfully");

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

        /*
           Performance Wise: Instead of using the commented code, the one that I wrote use a faster index verification.
           No entity mapping overhead!

           userRepo.findById(requestUserById)
                .orElseThrow(()-> new UsernameNotFoundException("username not found for delete : " + requestUserById));
        */

        if (!userRepo.existsById(requestUserById)) {
            throw new UsernameNotFoundException("User id not found for deletion : " + requestUserById);
        }

        userRepo.deleteById(requestUserById);
        requestRespondUser.setStatusCode(200);
        requestRespondUser.setMessage("User successfully deleted");

        return requestRespondUser;
    }

    private Users checkUserNewInformation(Users requestUpdateUser, Users user) {
        if(Objects.nonNull(requestUpdateUser.getFirstName()) && !requestUpdateUser.getFirstName().isEmpty()) {
            user.setFirstName(requestUpdateUser.getFirstName());
        }
        if(Objects.nonNull(requestUpdateUser.getLastName()) && !requestUpdateUser.getLastName().isEmpty()) {
            user.setLastName(requestUpdateUser.getLastName());
        }
        if(Objects.nonNull(requestUpdateUser.getDepartment()) && !requestUpdateUser.getDepartment().isEmpty()) {
            user.setDepartment(requestUpdateUser.getDepartment());
        }
        if(Objects.nonNull(requestUpdateUser.getEmail()) && !requestUpdateUser.getEmail().isEmpty()) {
            user.setEmail(requestUpdateUser.getEmail());
        }
        return user;
    }
}
