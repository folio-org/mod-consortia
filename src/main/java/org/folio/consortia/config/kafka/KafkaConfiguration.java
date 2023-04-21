package org.folio.consortia.config.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

  private final KafkaProperties kafkaProperties;

  @Bean
  public String kafkaEnvId(@Value("${ENV:folio}") String envId) {
    return envId;
  }
  @Bean
  public <V> ConcurrentKafkaListenerContainerFactory<String, V> kafkaListenerContainerFactory(ConsumerFactory<String, V> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, V>();
    factory.setConsumerFactory(consumerFactory);
    if (kafkaProperties.getListener().getAckMode() != null) {
      factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
    }
    return factory;
  }

  @Bean
  public <V> ConsumerFactory<String, V> consumerFactory(ObjectMapper objectMapper, FolioModuleMetadata folioModuleMetadata) {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      props.put("folioModuleMetadata", folioModuleMetadata);
      return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public <V> ProducerFactory<String, V> producerFactory(
      FolioExecutionContext folioExecutionContext) {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put("folioExecutionContext", folioExecutionContext);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public <V> KafkaTemplate<String, V> kafkaTemplate(ProducerFactory<String, V> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
