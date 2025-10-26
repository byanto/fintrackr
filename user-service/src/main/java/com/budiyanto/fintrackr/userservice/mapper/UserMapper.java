package com.budiyanto.fintrackr.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", source = "roles")
    LoginResponse toLoginResponse(User user);

    default String roleToString(Role role) {
        return role.getName();
    }
    
}
