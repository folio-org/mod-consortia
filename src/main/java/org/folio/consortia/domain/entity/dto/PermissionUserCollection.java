package org.folio.consortia.domain.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionUserCollection {

  private List<PermissionUser> permissionUsers;
  private Integer totalRecords;

}
