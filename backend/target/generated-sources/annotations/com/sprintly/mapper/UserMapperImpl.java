package com.sprintly.mapper;

import com.sprintly.dto.response.UserResponse;
import com.sprintly.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-01T09:22:05+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.fullName( user.getFullName() );
        userResponse.email( user.getEmail() );
        userResponse.avatarUrl( user.getAvatarUrl() );

        return userResponse.build();
    }
}
