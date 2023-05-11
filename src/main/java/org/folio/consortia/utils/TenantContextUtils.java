package org.folio.consortia.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
@Log4j2
public class TenantContextUtils {
  public static FolioExecutionContext getFolioExecutionContextCopyForTenant(FolioExecutionContext context,
                                                                            String tenant) {
    var headers = context.getAllHeaders() != null
      ? context.getAllHeaders()
      : new HashMap<String, Collection<String>>();
    headers.put(XOkapiHeaders.TENANT, Collections.singletonList(tenant));

    return new DefaultFolioExecutionContext(context.getFolioModuleMetadata(), headers);
  }

  public static FolioExecutionContext getFolioExecutionContextCreatePrimaryAffiliationEvent(MessageHeaders headers,
                                                                                            FolioModuleMetadata moduleMetadata,
                                                                                            String centralTenantId) {
    return getContextFromKafkaHeaders(headers, moduleMetadata, centralTenantId);
  }

  public static FolioExecutionContext getFolioExecutionContextDeletePrimaryAffiliationEvent(MessageHeaders headers,
                                                                                            FolioModuleMetadata moduleMetadata,
                                                                                            String centralTenantId) {
    return getContextFromKafkaHeaders(headers, moduleMetadata, centralTenantId);
  }

  //  use single thread,
  public static void runInFolioContext(FolioExecutionContext context, Runnable runnable) {
    try (var fec = new FolioExecutionContextSetter(context)) {
      runnable.run();
    }
  }

  private static FolioExecutionContext getContextFromKafkaHeaders(MessageHeaders headers,
                                                                  FolioModuleMetadata moduleMetadata, String centralTenantId) {
    // I should enable mod=config for all tenant. it is okay with remote
    Map<String, Collection<String>> map = new HashMap<>();
    map.put(XOkapiHeaders.TENANT, List.of(centralTenantId));
    map.put(XOkapiHeaders.URL, getHeaderValue(headers, XOkapiHeaders.URL, null));
    map.put(XOkapiHeaders.TOKEN, getHeaderValue(headers, XOkapiHeaders.TOKEN, null));
    map.put(XOkapiHeaders.USER_ID, getHeaderValue(headers, XOkapiHeaders.USER_ID, null));

    return new DefaultFolioExecutionContext(moduleMetadata, map);
  }

  public static List<String> getHeaderValue(MessageHeaders headers, String headerName, String defaultValue) {
    var headerValue = headers.get(headerName);
    var value = headerValue == null
      ? defaultValue
      : new String((byte[]) headerValue, StandardCharsets.UTF_8);
    return value == null ? Collections.emptyList() : Collections.singletonList(value);
  }

  public static FolioExecutionContext prepareContextForTenant(String tenantId, FolioExecutionContext context,
                                                              FolioModuleMetadata folioModuleMetadata) {
    if (MapUtils.isNotEmpty(context.getOkapiHeaders())) {
      context.getOkapiHeaders().put(XOkapiHeaders.TENANT, List.of(tenantId));
      log.info("FOLIO context initialized with tenant {}", tenantId);
    }
    return new DefaultFolioExecutionContext(folioModuleMetadata, context.getOkapiHeaders());
  }

}
