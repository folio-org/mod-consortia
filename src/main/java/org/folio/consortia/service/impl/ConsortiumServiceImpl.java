package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.ConsortiumService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class ConsortiumServiceImpl implements ConsortiumService {

  private final ConsortiumRepository consortiumRepository;

  @Override
  public void checkConsortiumExists(UUID consortiumId) {
    if (!consortiumRepository.existsById(consortiumId)) {
      throw new ResourceNotFoundException("consortiumId", String.valueOf(consortiumId));
    }
  }
}
