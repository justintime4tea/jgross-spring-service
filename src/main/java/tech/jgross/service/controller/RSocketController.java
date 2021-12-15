package tech.jgross.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import io.micrometer.core.instrument.config.validate.Validated.Invalid;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.publisher.Sinks.Many;
import tech.jgross.service.dto.GetMessagesRequest;
import tech.jgross.service.dto.MessageDto;
import tech.jgross.service.dto.RSockMessage;
import tech.jgross.service.dto.SendMessageRequest;
import tech.jgross.service.entity.Message;
import tech.jgross.service.service.MessageService;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketController {

  static final String USER_ID = "abc-xyz-123-456";
  static final String RESPONSE = "Response";
  static final String STREAM = "Stream";
  static final String MESSAGE = "Lorem ipsum forem.";

  private final List<RSocketRequester> CLIENTS = new ArrayList<>();

  @Autowired
  private final @NonNull MessageService messageService;

  private final Many<MessageDto> processor = Sinks.many().replay().all();

  @PreDestroy
  void shutdown() {
    log.info("Detaching all remaining clients...");
    CLIENTS.stream().forEach(requester -> requester.rsocket().dispose());
    log.info("Shutting down.");
  }

  @ConnectMapping("shell-client")
  void connectShellClientAndAskForTelemetry(RSocketRequester requester,
      @Payload String client) {

    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          // Add all new clients to a client list
          log.info("Client: {} CONNECTED.", client);
          CLIENTS.add(requester);
        })
        .doOnError(error -> {
          // Warn when channels are closed by clients
          log.warn("Channel to client {} CLOSED", client);
        })
        .doFinally(consumer -> {
          // Remove disconnected clients from the client list
          CLIENTS.remove(requester);
          log.info("Client {} DISCONNECTED", client);
        })
        .subscribe();

    // Callback to client, confirming connection
    requester.route("client-status")
        .data("OPEN")
        .retrieveFlux(String.class)
        .doOnNext(s -> log.info("Client: {} Free Memory: {}.", client, s))
        .subscribe();
  }
  
  /**
   * This @MessageMapping is intended to be used "request --> response" style. For
   * each RSockMessage received, a new RSockMessage is returned with ORIGIN=Server
   * and INTERACTION=Request-Response.
   *
   * @param request
   * @return Message
   */
  @PreAuthorize("hasRole('USER')")
  @MessageMapping("send-message")
  Mono<Message> requestResponse(final SendMessageRequest request, @AuthenticationPrincipal UserDetails user) {
    log.info("Received send-message request: {}", request);
    log.info("Request-response initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());
    Message newMessage = new Message(UUID.randomUUID().toString(), USER_ID, request.getMessage());
    this.processor.tryEmitNext(MessageDto.fromEntity(newMessage));
    // create a single RSockMessage and return it
    return messageService.createMessage(newMessage);
  }

  /**
   * This @MessageMapping is intended to be used "fire --> forget" style. When a
   * new CommandRequest is received, nothing is returned (void)
   *
   * @param request
   * @return
   */
  @PreAuthorize("hasRole('USER')")
  @MessageMapping("fire-and-forget")
  public Mono<Void> fireAndForget(final RSockMessage request, @AuthenticationPrincipal UserDetails user) {
    log.info("Received fire-and-forget request: {}", request);
    log.info("Fire-And-Forget initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());
    return Mono.empty();
  }

  /**
   * This @MessageMapping is intended to be used "subscribe --> stream" style.
   * When a new request command is received, a new stream of events is started and
   * returned to the client.
   *
   * @param request
   * @return
   */
  @PreAuthorize("hasRole('USER')")
  @MessageMapping("message-stream")
  Flux<MessageDto> stream(final RSockMessage request, @AuthenticationPrincipal UserDetails user) {
    log.info("Received stream request: {}", request);
    log.info("Stream initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());
    messageService.getMessages()
        .doOnNext(m -> log.info("sinking msg " + m))
        .map(MessageDto::fromEntity)
        .subscribe(m -> {
          this.processor.tryEmitNext(m);
        });
    return this.processor.asFlux();
  }

  /**
   * This @MessageMapping is intended to be used "stream <--> stream" style. The
   * incoming stream contains the interval settings (in seconds) for the outgoing
   * stream of messages.
   *
   * @param request
   * @return
   */
  @PreAuthorize("hasRole('USER')")
  @MessageMapping("messages")
  Flux<MessageDto> messages(final Flux<GetMessagesRequest> request, @AuthenticationPrincipal UserDetails user) {
    log.info("Received messages request...");
    log.info("Channel initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());
    return request
        .doOnNext(r -> log.info("Client request is {}", r.toString()))
        .doOnCancel(() -> log.warn("The client cancelled the channel."))
        .<Message>switchMap(r -> messageService.getMessages())
        .map(MessageDto::fromEntity);
  }
}
