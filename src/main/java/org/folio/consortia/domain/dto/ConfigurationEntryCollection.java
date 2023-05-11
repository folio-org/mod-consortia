package org.folio.consortia.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigurationEntryCollection {
  List<ConfigurationEntry> configs;
  Integer totalRecords;
}
