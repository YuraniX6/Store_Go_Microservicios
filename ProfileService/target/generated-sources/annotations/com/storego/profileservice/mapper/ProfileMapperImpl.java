package com.storego.profileservice.mapper;

import com.storego.profileservice.dto.ProfileResponse;
import com.storego.profileservice.dto.PublicProfileResponse;
import com.storego.profileservice.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-19T00:17:07-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public ProfileResponse toProfileResponse(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileResponse.ProfileResponseBuilder profileResponse = ProfileResponse.builder();

        profileResponse.userId( profile.getUserId() );
        profileResponse.fullname( profile.getFullname() );
        profileResponse.rut( profile.getRut() );
        profileResponse.language( profile.getLanguage() );
        profileResponse.description( profile.getDescription() );
        profileResponse.createdAt( profile.getCreatedAt() );
        profileResponse.updatedAt( profile.getUpdatedAt() );

        return profileResponse.build();
    }

    @Override
    public PublicProfileResponse toPublicProfileResponse(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        PublicProfileResponse.PublicProfileResponseBuilder publicProfileResponse = PublicProfileResponse.builder();

        publicProfileResponse.userId( profile.getUserId() );
        publicProfileResponse.fullname( profile.getFullname() );
        publicProfileResponse.language( profile.getLanguage() );
        publicProfileResponse.description( profile.getDescription() );
        publicProfileResponse.createdAt( profile.getCreatedAt() );

        return publicProfileResponse.build();
    }
}
