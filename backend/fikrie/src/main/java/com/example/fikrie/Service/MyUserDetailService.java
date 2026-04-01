package com.example.fikrie.Service;

import com.example.fikrie.Model.UserPrinciple;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

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
