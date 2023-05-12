package org.folio.consortia.domain.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PermissionUser {

  private String id;
  private String userId;
  private List<String> permissions;

}
