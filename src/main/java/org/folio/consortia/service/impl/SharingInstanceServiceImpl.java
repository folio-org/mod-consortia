package org.folio.consortia.service.impl;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.folio.consortia.repository.SharingInstanceRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.SharingInstanceService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SharingInstanceServiceImpl implements SharingInstanceService {

  private final SharingInstanceRepository sharingInstanceRepository;
  private final ConsortiumService consortiumService;
  private final ConversionService converter;

  @Override
  @Transactional
  public SharingInstance save(UUID consortiumId, SharingInstance sharedInstanceAction) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    SharingInstanceEntity savedSharedInstanceAction = sharingInstanceRepository.save(toEntity(sharedInstanceAction));
    return converter.convert(savedSharedInstanceAction, SharingInstance.class);
  }

  private SharingInstanceEntity toEntity(SharingInstance dto) {
    SharingInstanceEntity entity = new SharingInstanceEntity();
    entity.setInstanceId(dto.getInstanceIdentifier());
    entity.setSourceTenantId(dto.getSourceTenantId());
    entity.setTargetTenantId(dto.getTargetTenantId());
    entity.setStatus(SharingInstanceEntity.StatusType.IN_PROGRESS);
    return entity;
  }

}
