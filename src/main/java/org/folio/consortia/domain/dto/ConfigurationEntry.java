package org.folio.consortia.domain.dto;

import lombok.Data;

@Data
public class ConfigurationEntry {
  String id;
  String module;
  String configName;
  String value;
}
