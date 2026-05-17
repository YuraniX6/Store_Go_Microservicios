package com.storego.profileservice.mapper;

import com.storego.profileservice.dto.ProfileResponse;
import com.storego.profileservice.dto.PublicProfileResponse;
import com.storego.profileservice.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponse toProfileResponse(Profile profile);

    PublicProfileResponse toPublicProfileResponse(Profile profile);
}
