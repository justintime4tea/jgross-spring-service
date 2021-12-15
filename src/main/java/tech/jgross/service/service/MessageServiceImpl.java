package tech.jgross.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jgross.service.entity.Message;
import tech.jgross.service.repository.MessageRepository;


@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
  @Autowired
  private final @NonNull MessageRepository messageRepo;

  @Override
  public Mono<Message> createMessage(Message message) {
    return messageRepo.createMessage(message);
  }

  @Override
  public Mono<Message> getMessage(String id) {
    return messageRepo.getMessage(id);
  }

  @Override
  public Mono<Message> updateMessage(String id, Message message) {
    return messageRepo.updateMessage(id, message);
  }

  @Override
  public Mono<Void> deleteMessage(String id) {
    return messageRepo.deleteMessage(id);
  }

  @Override
  public Flux<Message> getMessages() {
    return this.messageRepo.getMessages(25).asFlux();
  }
}