package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SharingInstanceConverter implements Converter<SharingInstanceEntity, SharingInstance> {

  @Override
  public SharingInstance convert(SharingInstanceEntity source) {
    SharingInstance sharingInstanceAction = new SharingInstance();
    sharingInstanceAction.setId(source.getId());
    sharingInstanceAction.setInstanceIdentifier(source.getId());
    sharingInstanceAction.setSourceTenantId(source.getSourceTenantId());
    sharingInstanceAction.setTargetTenantId(source.getTargetTenantId());
    sharingInstanceAction.setStatus(String.valueOf(source.getStatus()));
    sharingInstanceAction.setError(source.getError());
    return sharingInstanceAction;
  }
}
