package org.folio;

import org.folio.entity.Tenant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EntityScan(basePackageClasses = Tenant.class)
public class FolioConsortiaApplication {

  public static void main(String[] args) {
    SpringApplication.run(FolioConsortiaApplication.class, args);
  }

 }
