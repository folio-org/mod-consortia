package org.folio.consortia.domain.mapper;

import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.entity.TenantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TenantMapper {

  TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);

  Tenant toDto(TenantEntity tenantEntity);
  TenantEntity toEntity(Tenant tenant);

}
