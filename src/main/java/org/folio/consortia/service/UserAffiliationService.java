package org.folio.consortia.service;

public interface UserAffiliationService {
  void createPrimaryUserAffiliation(String userTenant);
  void deletePrimaryUserAffiliation(String data);
}
