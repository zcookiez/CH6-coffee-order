package com.sparta.coffee.domain.user.service;

import com.sparta.coffee.domain.user.dto.UserResponse;
import com.sparta.coffee.domain.user.dto.UserSignupRequest;
import com.sparta.coffee.domain.user.entity.User;
import com.sparta.coffee.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        User user = User.builder()
                .name(request.name())
                .build();
        
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
    }
}
