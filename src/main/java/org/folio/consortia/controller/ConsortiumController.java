package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.domain.dto.ConsortiumCollection;
import org.folio.consortia.rest.resource.ConsortiaApi;
import org.folio.consortia.service.ConsortiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConsortiumController implements ConsortiaApi {
  @Autowired
  ConsortiumService consortiumService;

  @Override
  public ResponseEntity<Consortium> saveConsortium(Consortium consortium) {
    return new ResponseEntity<>(consortiumService.save(consortium), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Consortium> getConsortium(UUID consortiumId) {
    return new ResponseEntity<>(consortiumService.get(consortiumId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Consortium> updateConsortium(UUID consortiumId, Consortium consortium) {
    return new ResponseEntity<>(consortiumService.update(consortiumId, consortium), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ConsortiumCollection> getConsortiumCollection() {
    return  new ResponseEntity<>(consortiumService.getAll(), HttpStatus.OK);
  }
}
