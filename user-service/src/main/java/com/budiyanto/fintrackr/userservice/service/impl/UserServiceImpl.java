package com.budiyanto.fintrackr.userservice.service.impl;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.dto.UserUpdateRequest;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .map(userMapper::toUserResponse)
            .orElseThrow(() ->
                new UserNotFoundException(
                    "User not found: %s".formatted(username)
                )
            );
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest request) {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found: \"%s\".".formatted(username)));

        // Check for email conflict if email is being updated
        if (
            request.email() != null &&
            !user.getEmail().equalsIgnoreCase(request.email())
        ) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new UserAlreadyExistsException(
                    "Email %s is already in use.".formatted(request.email())
                );
            }
        }

        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }
}
