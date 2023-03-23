package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.domain.entity.ConsortiumEntity;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.service.ConsortiumService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ConsortiumServiceImpl implements ConsortiumService {
  private static final String CONSORTIUM_RESOURCE_EXIST_MSG_TEMPLATE = "System can not have more than one consortium record";

  private final ConsortiumRepository repository;
  private final ConversionService converter;

  @Override
  public Consortium save(Consortium consortiumDto) {
    checkConsortiumNotExistsOrThrow();
    ConsortiumEntity entity = new ConsortiumEntity();
    entity.setId(consortiumDto.getId());
    entity.setName(consortiumDto.getName());
    ConsortiumEntity consortiumEntity = repository.save(entity);
    return converter.convert(consortiumEntity, Consortium.class);
  }

  private void checkConsortiumNotExistsOrThrow() {
    if (repository.count() > 0) {
      throw new ResourceAlreadyExistException(CONSORTIUM_RESOURCE_EXIST_MSG_TEMPLATE);
    }
  }
}
