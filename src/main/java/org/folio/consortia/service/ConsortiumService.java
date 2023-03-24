package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.domain.dto.ConsortiumCollection;
import org.folio.consortia.domain.entity.ConsortiumEntity;

import java.util.UUID;

public interface ConsortiumService {
/*
ConsortiumService SAVE() Can be used to store one and only one record.
*/
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

/*
ConsortiumService getAll() Can return one and only one record.
*/
  /**
   * Gets consortiums.
   *
   * @return consortiums collection
   */
  ConsortiumCollection getAll();

  ConsortiumEntity checkConsortiumExistsOrThrow(UUID consortiumId);
}
