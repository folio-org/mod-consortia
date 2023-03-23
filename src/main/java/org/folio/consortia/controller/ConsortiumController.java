package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.rest.resource.ConsortiaApi;
import org.folio.consortia.service.ConsortiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConsortiumController implements ConsortiaApi {
  @Autowired
  ConsortiumService consortiumService;

  @Override
  @PostMapping("/consortia")
  public ResponseEntity<Consortium> saveConsortium(Consortium consortium) {
    return ResponseEntity.ok(consortiumService.save(consortium));
  }
}
