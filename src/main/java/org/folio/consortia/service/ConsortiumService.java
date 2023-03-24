package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.domain.dto.ConsortiumCollection;

import java.util.UUID;

public interface ConsortiumService {

  /**
   * Inserts single consortium.
   *
   * @param consortiumDto  the consortiumDto
   * @return consortiumDto
   */
  Consortium save(Consortium consortiumDto);

  /**
   * Gets consortium based on consortiumId.
   *
   * @param consortiumId  the consortiumId
   * @return consortiumDto
   */
  Consortium get(UUID consortiumId);

  /**
   * Updates single consortium based on consortiumId.
   *
   * @param consortiumId  the consortiumId
   * @param consortiumDto  the consortiumDto
   * @return consortiumDto
   */
  Consortium update(UUID consortiumId, Consortium consortiumDto);

  /**
   * Gets consortiums.
   *
   * @return consortiums collection
   */
  ConsortiumCollection getAll();
}
