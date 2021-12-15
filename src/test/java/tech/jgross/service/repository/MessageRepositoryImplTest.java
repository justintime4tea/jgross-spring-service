package tech.jgross.service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;
import tech.jgross.service.entity.Message;


@SpringBootTest
public class MessageRepositoryImplTest {
  private Map<String, Message> messageStorage = new HashMap<String, Message>();
  private MessageRepository messageRepository;

  @BeforeEach
  void init() {
    messageRepository = new MessageRepositoryImpl(messageStorage);
  }

  @AfterEach
  void tearDown() {
    messageStorage.clear();
  }

  @ParameterizedTest
  @MethodSource("provideMessageServiceArgs")
  public void constructMessageService(Map<String, Message> messageRepo) {
    new MessageRepositoryImpl(messageRepo);
  }

  @ParameterizedTest
  @MethodSource("provideMessage")
  public void createMessageReturnsMessageJustCreated(Message message) {
    assertNull(messageStorage.get(message.id()));

    StepVerifier.create(messageRepository.createMessage(message))
        .expectNext(message)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void getMessageReturnsMessageWhenExists(String id) {
    Message persistedMessage = new Message(id, "jane doe", "message");
    messageStorage.put(id, persistedMessage);

    StepVerifier.create(messageRepository.getMessage(id))
        .expectNext(persistedMessage)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void getMessageReturnsEmptyWhenDoesntExist(String id) {
    StepVerifier.create(messageRepository.getMessage(id))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void getMessages() {
    String idOne = "89c07e18-3876-4b1e-9ea2-e0731f3d6932";
    String idTwo = "8421a9be-f322-4d46-b388-5bce3d5e2211";

    Message persistedMessageOne = new Message(idOne, "john doe", "message");
    Message persistedMessageTwo = new Message(idTwo, "jane doe", "message");
    messageStorage.put(idOne.toString(), persistedMessageOne);
    messageStorage.put(idTwo.toString(), persistedMessageTwo);

    List<Message> messages = messageRepository.getMessages().collectList().block(Duration.ofSeconds(5));
    assertTrue(messages.contains(persistedMessageOne));
    assertTrue(messages.contains(persistedMessageTwo));
  }

  @ParameterizedTest
  @MethodSource({ "provideUpdateArguments" })
  public void updateMessage(String id, Message message) {
    Message persistedMessage = new Message(id, "jane doe", "message");
    messageStorage.put(id, message);
    messageRepository.updateMessage(id, message);
    assertNotSame(messageStorage.get(id), persistedMessage);
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void deleteMessage(String id) {
    Message persistedMessage = new Message(id, "john doe", "message");
    messageStorage.put(id, persistedMessage);
    assertEquals(persistedMessage, messageStorage.get(id));
    messageRepository.deleteMessage(id);
    assertNull(messageStorage.get(id));
  }

  private static Stream<Message> provideMessage() {
    return Stream.of(
        new Message("3a7b729b-03fc-45ca-9900-6038b303f40d", "john doe", "hello"));
  }

  private static Stream<String> provideUuid() {
    return Stream.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private static Stream<Arguments> provideUpdateArguments() {
    String id = UUID.randomUUID().toString();
    return Stream.of(Arguments.of(id, new Message(id, "john doe", "hello")));
  }

  private static Stream<Map<UUID, Message>> provideMessageServiceArgs() {
    return Stream.of(new HashMap<UUID, Message>());
  }
}
