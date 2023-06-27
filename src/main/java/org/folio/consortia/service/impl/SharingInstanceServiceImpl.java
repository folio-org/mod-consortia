package org.folio.consortia.service.impl;

import static org.folio.consortia.repository.SharingInstanceRepository.Specifications.constructSpecification;

import java.util.UUID;
import jakarta.transaction.Transactional;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.domain.dto.SharingInstanceCollection;
import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.SharingInstanceRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.SharingInstanceService;
import org.folio.consortia.service.TenantService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SharingInstanceServiceImpl implements SharingInstanceService {

  private final SharingInstanceRepository sharingInstanceRepository;
  private final ConsortiumService consortiumService;
  private final TenantService tenantService;
  private final ConversionService converter;

  @Override
  public SharingInstance getById(UUID consortiumId, UUID actionId) {
    log.debug("getById:: Trying to get by consortiumId: {} and action id: {}", consortiumId, actionId);
    SharingInstanceEntity sharingInstanceEntity = sharingInstanceRepository.findById(actionId).
      orElseThrow(() -> new ResourceNotFoundException("actionId", String.valueOf(actionId)));
    log.info("getById:: SharedInstance object: {} was successfully retrieved", sharingInstanceEntity.getId());
    return converter.convert(sharingInstanceEntity, SharingInstance.class);
  }

  @Override
  @Transactional
  public SharingInstance start(UUID consortiumId, SharingInstance sharingInstance) {
    log.debug("start:: Trying to start instance sharing: {} for consortium: {}", sharingInstance.getInstanceIdentifier(), consortiumId);
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    tenantService.checkTenantExistsOrThrow(sharingInstance.getSourceTenantId());
    tenantService.checkTenantExistsOrThrow(sharingInstance.getTargetTenantId());
    SharingInstanceEntity savedSharingInstance = sharingInstanceRepository.save(toEntity(sharingInstance));
    log.info("start:: SharingInstance '{}' with instanceId '{}', sourceTenantId '{}', targetTenantId '{}'  successfully saved in db",
      savedSharingInstance.getId(), savedSharingInstance.getInstanceId(), savedSharingInstance.getSourceTenantId(), savedSharingInstance.getTargetTenantId());
    return converter.convert(savedSharingInstance, SharingInstance.class);
  }

  @Override
  public SharingInstanceCollection getSharingInstances(UUID consortiumId, UUID instanceIdentifier, String sourceTenantId,
      String targetTenantId, String status, Integer offset, Integer limit) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var specification = constructSpecification(instanceIdentifier, sourceTenantId, targetTenantId, status);

    var sharingInstancePage = sharingInstanceRepository.findAll(specification, PageRequest.of(offset, limit));
    var result = new SharingInstanceCollection();
    result.setSharingInstances(sharingInstancePage.stream().map(o -> converter.convert(o, SharingInstance.class)).toList());
    result.setTotalRecords((int) sharingInstancePage.getTotalElements());
    return result;
  }

  private SharingInstanceEntity toEntity(SharingInstance dto) {
    SharingInstanceEntity entity = new SharingInstanceEntity();
    entity.setId(UUID.randomUUID());
    entity.setInstanceId(dto.getInstanceIdentifier());
    entity.setSourceTenantId(dto.getSourceTenantId());
    entity.setTargetTenantId(dto.getTargetTenantId());
    entity.setStatus(SharingInstanceEntity.StatusType.IN_PROGRESS);
    return entity;
  }
}
