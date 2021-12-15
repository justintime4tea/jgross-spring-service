package tech.jgross.service.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import tech.jgross.service.entity.Message;


public interface MessageRepository {
  public abstract Mono<Message> createMessage(Message message);

  public abstract Mono<Message> getMessage(String id);

  public abstract Mono<Message> updateMessage(String id, Message message);

  public abstract Mono<Void> deleteMessage(String id);

  public abstract Flux<Message> getMessages();

  public abstract Sinks.Many<Message> getMessages(int historySize);
}
