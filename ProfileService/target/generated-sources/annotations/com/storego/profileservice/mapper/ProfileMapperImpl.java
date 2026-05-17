package com.storego.profileservice.mapper;

import com.storego.profileservice.dto.ProfileResponse;
import com.storego.profileservice.dto.PublicProfileResponse;
import com.storego.profileservice.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-17T11:39:37-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public ProfileResponse toProfileResponse(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileResponse.ProfileResponseBuilder profileResponse = ProfileResponse.builder();

        profileResponse.createdAt( profile.getCreatedAt() );
        profileResponse.description( profile.getDescription() );
        profileResponse.fullname( profile.getFullname() );
        profileResponse.language( profile.getLanguage() );
        profileResponse.rut( profile.getRut() );
        profileResponse.updatedAt( profile.getUpdatedAt() );
        profileResponse.userId( profile.getUserId() );

        return profileResponse.build();
    }

    @Override
    public PublicProfileResponse toPublicProfileResponse(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        PublicProfileResponse.PublicProfileResponseBuilder publicProfileResponse = PublicProfileResponse.builder();

        publicProfileResponse.createdAt( profile.getCreatedAt() );
        publicProfileResponse.description( profile.getDescription() );
        publicProfileResponse.fullname( profile.getFullname() );
        publicProfileResponse.language( profile.getLanguage() );
        publicProfileResponse.userId( profile.getUserId() );

        return publicProfileResponse.build();
    }
}
