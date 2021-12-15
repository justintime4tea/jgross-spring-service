package tech.jgross.service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.jgross.service.entity.Message;
import tech.jgross.service.repository.MessageRepository;
import tech.jgross.service.repository.MessageRepositoryImpl;


@SpringBootTest
public class MessageServiceImplTest {
  @MockBean
  private MessageRepository messageRepo;

  private MessageService messageService;

  @BeforeEach
  void init() {
    messageService = new MessageServiceImpl(messageRepo);
  }

  @ParameterizedTest
  @MethodSource("provideMessageServiceArgs")
  public void constructMessageService(MessageRepository messageRepo) {
    new MessageServiceImpl(messageRepo);
  }

  @ParameterizedTest
  @MethodSource("provideMessage")
  public void createMessageReturnsMessageJustCreated(Message message) {
    when(messageRepo.createMessage(any()))
        .thenReturn((Mono<Message>) Mono.just(message));

    StepVerifier.create(messageService.createMessage(message))
        .expectNext(message)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void getMessageReturnsMessageWhenExists(String id) {
    Message persistedMessage = new Message(id, "jane doe", "message");
    when(messageRepo.getMessage(anyString()))
        .thenReturn((Mono<Message>) Mono.just(persistedMessage));

    StepVerifier.create(messageService.getMessage(id))
        .expectNext(persistedMessage)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void getMessageReturnsEmptyWhenDoesntExist(String id) {
    when(messageRepo.getMessage(anyString()))
        .thenReturn(Mono.empty());

    StepVerifier.create(messageService.getMessage(id))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void getMessages() {
    String idOne = "15903266-1c8d-49bf-a188-1640153711a5";
    String idTwo = "f8bfbc8b-c423-4bbc-a787-502af060648f";
    Message persistedMessageOne = new Message(idOne, "john doe", "message");
    Message persistedMessageTwo = new Message(idTwo, "jane doe", "message");

    when(messageRepo.getMessages())
        .thenReturn((Flux<Message>) Flux.just(persistedMessageOne, persistedMessageTwo));

    StepVerifier.create(messageService.getMessages())
        .expectNext(persistedMessageOne)
        .expectNext(persistedMessageTwo)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource({ "provideUpdateArguments" })
  public void updateMessage(String id, Message message) {
    when(messageRepo.updateMessage(anyString(), any()))
        .thenReturn((Mono<Message>) Mono.just(message));

    StepVerifier.create(messageService.updateMessage(id, message))
        .expectNext(message)
        .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void deleteMessage(String id) {
    when(messageRepo.deleteMessage(anyString()))
        .thenReturn(Mono.empty());

    StepVerifier.create(messageService.deleteMessage(id))
        .expectNextCount(0)
        .verifyComplete();
  }

  private static Stream<Message> provideMessage() {
    String id = UUID.randomUUID().toString();
    String sender = UUID.randomUUID().toString();
    String data = UUID.randomUUID().toString();
    return Stream.of(
        new Message(id, sender, data));
  }

  private static Stream<String> provideUuid() {
    return Stream.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private static Stream<Arguments> provideUpdateArguments() {
    String id = UUID.randomUUID().toString();
    String sender = UUID.randomUUID().toString();
    String data = UUID.randomUUID().toString();
    return Stream.of(Arguments.of(id, new Message(id, sender, data)));
  }

  private static Stream<MessageRepositoryImpl> provideMessageServiceArgs() {
    return Stream.of(new MessageRepositoryImpl(new HashMap<String, Message>()));
  }
}
