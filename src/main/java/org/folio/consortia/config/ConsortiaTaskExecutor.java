package org.folio.consortia.config;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ConsortiaTaskExecutor implements Runnable {
  @Override
  @Async
  public void run() {

  }
}
