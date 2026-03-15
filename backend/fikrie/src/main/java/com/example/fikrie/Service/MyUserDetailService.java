package com.example.fikrie.Service;

import com.example.fikrie.Model.UserPrinciple;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> optionalUser = userRepo.findByUsername(username);
        if(optionalUser.isPresent()) {
            Users user = optionalUser.get();
            return new UserPrinciple(user);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
