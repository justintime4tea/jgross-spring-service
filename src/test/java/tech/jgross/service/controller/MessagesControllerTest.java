package tech.jgross.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jgross.service.dto.MessageDto;
import tech.jgross.service.entity.Message;
import tech.jgross.service.service.MessageService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessagesControllerTest {

	@Autowired
	private WebTestClient client;

	@MockBean
	private MessageService messageService;

	@ParameterizedTest
	@MethodSource("provideMessageDto")
	public void postMessageReturnsAcceptedAndMessage(MessageDto messageDto) throws Exception {
		when(messageService.createMessage(any()))
				.thenReturn((Mono<Message>) Mono.just(MessageDto.toEntity(messageDto)));

		client.post()
				.uri("/messages")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(messageDto), MessageDto.class)
				.exchange()
				.expectStatus()
				.isCreated()
				.expectBody(MessageDto.class)
				.consumeWith(r -> assertEquals(messageDto, r.getResponseBody()));
	}

	@ParameterizedTest
	@MethodSource("provideMessageDto")
	public void postMessageReturnsBadRequestUponBadRequest(MessageDto messageDto) throws Exception {
		when(messageService.createMessage(any()))
				.thenReturn((Mono<Message>) Mono.just(MessageDto.toEntity(messageDto)));

		client.post()
				.uri("/messages")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(new BadJsonPayload()), BadJsonPayload.class)
				.exchange()
				.expectStatus()
				.isBadRequest()
				.expectBody()
				.consumeWith(e -> System.out.println(e.toString()));
	}

	@ParameterizedTest
	@MethodSource("provideMessages")
	public void getMessagesReturnsMessages(List<Message> messages) throws Exception {
		when(messageService.getMessages())
				.thenReturn((Flux<Message>) Flux.fromIterable(messages));

		client.get()
				.uri("/messages")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBodyList(MessageDto.class)
				.consumeWith(m -> {
					List<MessageDto> returnedMessages = m.getResponseBody();
					assertEquals(messages.size(), returnedMessages.size());
					for (MessageDto message : returnedMessages) {
						messages.contains(MessageDto.toEntity(message));
					}
				});
	}

	@Test
	public void getMessagesReturnsEmptyListOfMessagesWhenThereAreNone() throws Exception {
		when(messageService.getMessages())
				.thenReturn(Flux.empty());

		client.get()
				.uri("/messages")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNoContent()
				.expectBodyList(MessageDto.class)
				.consumeWith(m -> {
					List<MessageDto> returnedMessages = m.getResponseBody();
					assertEquals(0, returnedMessages.size());
				});
	}

	@ParameterizedTest
	@MethodSource("provideMessage")
	public void getMessageReturnsMessage(Message message) throws Exception {

		when(messageService.getMessage(any()))
				.thenReturn((Mono<Message>) Mono.just(message));

		client.get()
				.uri(String.format("/messages/%s", message.id().toString()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(MessageDto.class)
				.consumeWith(r -> assertEquals(MessageDto.fromEntity(message), r.getResponseBody()));
	}

	@ParameterizedTest
	@MethodSource("provideMessage")
	public void getMessageReturnsNotFoundWhenMessageNotExist(Message message) throws Exception {

		when(messageService.getMessage(any()))
				.thenReturn(Mono.empty());

		client.get()
				.uri(String.format("/messages/%s", message.id().toString()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNotFound();
	}

	@Test
	public void getMessageReturnsNotFoundWhenUuidIsNotValid() throws Exception {
		client.get()
				.uri("/messages/not-a-uuid")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNotFound();
	}

	@ParameterizedTest
	@MethodSource("provideMessageDto")
	public void putMessageCreatesMessageWhenMessageDoesntExists(MessageDto messageDto) throws Exception {
		when(messageService.getMessage(any()))
				.thenReturn(Mono.empty());

		when(messageService.updateMessage(any(), any()))
				.thenReturn(Mono.empty());

		when(messageService.createMessage(any()))
				.thenReturn((Mono<Message>) Mono.just(MessageDto.toEntity(messageDto)));

		client.put()
				.uri(String.format("/messages/%s", messageDto.id()))
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(messageDto), MessageDto.class)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(MessageDto.class)
				.consumeWith(r -> assertEquals(messageDto, r.getResponseBody()));

		verify(messageService, times(1)).createMessage(MessageDto.toEntity(messageDto));
		verify(messageService, times(0)).updateMessage(any(), any());
	}

	@ParameterizedTest
	@MethodSource("provideMessageDto")
	public void putMessageUpdatesMessageWhenMessageExists(MessageDto messageDto) throws Exception {
		when(messageService.getMessage(anyString())).thenReturn(Mono.just(MessageDto.toEntity(messageDto)));

		when(messageService.updateMessage(anyString(), any()))
				.thenReturn((Mono<Message>) Mono.just(MessageDto.toEntity(messageDto)));

		when(messageService.createMessage(any()))
				.thenReturn(Mono.empty());

		client.put()
				.uri(String.format("/messages/%s", messageDto.id()))
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(messageDto), MessageDto.class)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(MessageDto.class)
				.consumeWith(r -> assertEquals(messageDto, r.getResponseBody()));

		verify(messageService, times(0)).createMessage(MessageDto.toEntity(messageDto));
		verify(messageService, times(1)).updateMessage(anyString(), any());
	}

	@ParameterizedTest
	@MethodSource("provideMessageDto")
	public void putMessageReturnsBadRequestWhenSupplyingNonUuuid(MessageDto messageDto) throws Exception {
		client.put()
				.uri("/messages/not-a-uuid")
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(messageDto), MessageDto.class)
				.exchange()
				.expectStatus()
				.isBadRequest();
	}

	@Test
	public void deleteMessageReturnsAccepted() throws Exception {
		when(messageService.deleteMessage(anyString())).thenReturn(Mono.empty());

		client.delete()
				.uri(String.format("/messages/%s", UUID.randomUUID().toString()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isAccepted();
	}

	@Test
	public void deleteMessageReturnsAcceptedEvenWhenIdIsNotUuid() throws Exception {
		when(messageService.deleteMessage(anyString())).thenReturn(Mono.empty());

		client.delete()
				.uri("/messages/not-a-uuid")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isAccepted();
	}

	private static Stream<Message> provideMessage() {
		return Stream.of(
				new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
				new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
				new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()));
	}

	private static Stream<MessageDto> provideMessageDto() {
		return Stream.of(
				new MessageDto(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
				new MessageDto(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
				new MessageDto(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()));
	}

	private static Stream<List<Message>> provideMessages() {
		return Stream.of(
				Arrays.asList(new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString())),
				Arrays.asList(new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString())),
				Arrays.asList(new Message(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString()),
						new Message(
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString(),
								UUID.randomUUID().toString())));
	}
}

final class BadJsonPayload {
	final public String foobar = "";
	final public String bazbuzz = "";
}