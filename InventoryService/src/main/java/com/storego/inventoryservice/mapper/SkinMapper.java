package com.storego.inventoryservice.mapper;

import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.entity.Skin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkinMapper {
    SkinResponse skinToSkinResponse(Skin skin);
}
