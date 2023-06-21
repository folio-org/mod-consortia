package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.dto.SharingInstanceAction;
import org.folio.consortia.domain.entity.SharingInstanceActionEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SharingInstanceActionConverter implements Converter<SharingInstanceActionEntity, SharingInstanceAction> {

  @Override
  public SharingInstanceAction convert(SharingInstanceActionEntity source) {
    SharingInstanceAction sharingInstanceAction = new SharingInstanceAction();
    sharingInstanceAction.setId(source.getId());
    sharingInstanceAction.setInstanceIdentifier(source.getId());
    sharingInstanceAction.setSourceTenantId(source.getSourceTenantId());
    sharingInstanceAction.setTargetTenantId(source.getTargetTenantId());
    sharingInstanceAction.setStatus(String.valueOf(source.getStatus()));
    sharingInstanceAction.setError(source.getError());
    return sharingInstanceAction;
  }
}
