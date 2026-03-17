package com.example.fikrie.DataTransferObject;

import com.example.fikrie.Model.Users;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestRespondUser {

    private int statusCode;
    private String username;
    private String password;
    private String role;
    private String email;
    private String refreshToken;
    private String expirationTime;
    private String token;
    private String message;
    private String error;
    private Users user;
    private List<Users> listOfUsers;
}
