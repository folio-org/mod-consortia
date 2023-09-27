package org.folio.consortia.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
  private String permissionName;
  private String displayName;
  private String description;

  public Permission(String permissionName) {
    this.permissionName = permissionName;
  }
}
