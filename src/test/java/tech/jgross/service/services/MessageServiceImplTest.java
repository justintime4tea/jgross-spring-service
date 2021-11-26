package tech.jgross.service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import tech.jgross.service.entity.Message;


@SpringBootTest
@AutoConfigureMockMvc
public class MessageServiceImplTest {
  private Map<UUID, Message> messageStorage = anyMap();
  private MessageService messageService;

  @BeforeEach
  void init() {
    messageService = new MessageServiceImpl(messageStorage);
  }

  @AfterEach
  void tearDown() {
    messageStorage.clear();
  }

  @ParameterizedTest
  @MethodSource("provideMessageServiceArgs")
  public void constructMessageService(Map<UUID, Message> messageRepo) {
    new MessageServiceImpl(messageRepo);
  }

  @ParameterizedTest
  @MethodSource("provideMessage")
  public void createMessage(Message message) {
    assertNull(messageStorage.get(message.id()));
    messageService.createMessage(message);
    assertEquals(message, messageStorage.get(message.id()));
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void getMessage(UUID uuid) {
    Message persistedMessage = new Message(uuid, "jane doe", "message");
    messageStorage.put(uuid, persistedMessage);
    Message message = messageService.getMessage(uuid);
    assertEquals(message, persistedMessage);
  }

  @ParameterizedTest
  @MethodSource({ "provideUpdateArguments" })
  public void updateMessage(UUID uuid, Message message) {
    Message persistedMessage = new Message(uuid, "jane doe", "message");
    messageStorage.put(uuid, message);
    messageService.updateMessage(uuid, message);
    assertNotSame(messageStorage.get(uuid), persistedMessage);
  }

  @ParameterizedTest
  @MethodSource("provideUuid")
  public void deleteMessage(UUID uuid) {
    Message persistedMessage = new Message(uuid, "john doe", "message");
    messageStorage.put(uuid, persistedMessage);
    assertEquals(persistedMessage, messageStorage.get(uuid));
    messageService.deleteMessage(uuid);
    assertNull(messageStorage.get(uuid));
  }

  private static Stream<Message> provideMessage() {
    return Stream.of(
        new Message(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee91"), "john doe", "hello"));
  }

  private static Stream<UUID> provideUuid() {
    return Stream.of(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee91"));
  }

  private static Stream<Arguments> provideUpdateArguments() {
    UUID id = UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee91");
    return Stream.of(Arguments.of(id, new Message(id, "john doe", "hello")));
  }

  private static Stream<Map<UUID, Message>> provideMessageServiceArgs() {
    return Stream.of(new HashMap<UUID, Message>());
  }
}
