package tech.jgross.service.dto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

import org.springframework.beans.factory.config.BeanDefinition;


@Configuration
public class MessageDtoConfig {
  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public MessageDto messageDto(String userId, String message) {
    return new MessageDto(UUID.randomUUID().toString(), userId, message);
  }
}