package com.interswitch.submanager.config;

import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.models.enums.RoleType;
import com.interswitch.submanager.models.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SetupDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        if(userRepository.findUserByEmail("ernestehigiator@gmail.com").isEmpty()){
            User user = new User("Ernest", "Ehigiator","ernestehigiator@gmail.com", passwordEncoder.encode("password1234#"), "07081649157", RoleType.ROLE_ADMIN);
            user.setCreatedDate(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
