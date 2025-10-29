package com.budiyanto.fintrackr.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;

@Mapper(componentModel = "spring", uses = PasswordEncodingMapper.class)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedBy = EncodedMapping.class)
    User toUser(RegisterRequest registerRequest);

    UserResponse toUserResponse(User user);
}
