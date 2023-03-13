package org.folio.consortia.domain.mapper;

import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserTenantMapper {
  UserTenantMapper INSTANCE = Mappers.getMapper(UserTenantMapper.class);

  @Mapping(target = "tenantId", source = "tenant.id")
  UserTenant toDto(UserTenantEntity userTenantEntity);

  @Mapping(target = "tenant.id", source = "tenantId")
  UserTenantEntity toEntity(UserTenant userTenant);

}
