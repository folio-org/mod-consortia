package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Consortium;

public interface ConsortiumService {

  /**
   * Inserts single consortium.
   *
   * @param consortiumDto  the consortiumDto
   * @return consortiumDto
   */
  Consortium save(Consortium consortiumDto);
}
