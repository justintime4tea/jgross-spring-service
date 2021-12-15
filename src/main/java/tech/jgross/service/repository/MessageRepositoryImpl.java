package tech.jgross.service.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import tech.jgross.service.entity.Message;


@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {
  private final @NonNull Map<String, Message> messageStorage;
  private Sinks.Many<Message> messageSink = Sinks.many().replay().all();

  @Override
  public Mono<Message> createMessage(Message message) {
    messageStorage.put(message.id().toString(), message);
    messageSink.tryEmitNext(message);
    return Mono.just(message);
  }

  @Override
  public Mono<Message> getMessage(String id) {
    Message message = messageStorage.get(id);
    if (message == null) {
      return Mono.empty();
    }

    return Mono.just(message);
  }

  @Override
  public Mono<Message> updateMessage(String id, Message message) {
    messageStorage.put(id, message);
    return Mono.just(message);
  }

  @Override
  public Mono<Void> deleteMessage(String id) {
    messageStorage.remove(id);
    return Mono.empty();
  }

  @Override
  public Flux<Message> getMessages() {
    return Flux.fromStream(messageStorage.values().stream());
  }

  @Override
  public Sinks.Many<Message> getMessages(int historySize) {
    Sinks.Many<Message> sink = Sinks.many().replay().limit(historySize);
    emitToSink(this.messageStorage.values(), sink);
    return sink;
  }

  private void emitToSink(Iterable<Message> messages, Sinks.Many<Message> sink) {
    for (Message message : messages) {
      sink.tryEmitNext(message);
    }
  }
}
