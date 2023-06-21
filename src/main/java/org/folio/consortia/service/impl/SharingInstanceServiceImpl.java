package org.folio.consortia.service.impl;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstanceAction;
import org.folio.consortia.domain.entity.SharingInstanceActionEntity;
import org.folio.consortia.repository.SharingInstanceActionRepository;
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

  private final SharingInstanceActionRepository sharingInstanceActionRepository;
  private final ConsortiumService consortiumService;
  private final ConversionService converter;

  @Override
  @Transactional
  public SharingInstanceAction save(UUID consortiumId, SharingInstanceAction sharedInstanceAction) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    SharingInstanceActionEntity savedSharedInstanceAction = sharingInstanceActionRepository.save(toEntity(sharedInstanceAction));
    return converter.convert(savedSharedInstanceAction, SharingInstanceAction.class);
  }

  private SharingInstanceActionEntity toEntity(SharingInstanceAction dto) {
    SharingInstanceActionEntity entity = new SharingInstanceActionEntity();
    entity.setInstanceId(dto.getInstanceIdentifier());
    entity.setSourceTenantId(dto.getSourceTenantId());
    entity.setTargetTenantId(dto.getTargetTenantId());
    entity.setStatus(SharingInstanceActionEntity.StatusType.IN_PROGRESS);
    return entity;
  }

}
