package tech.jgross.service.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jgross.service.dto.MessageDto;
import tech.jgross.service.entity.Message;
import tech.jgross.service.service.MessageService;


@RestController
@Validated
@RequiredArgsConstructor
public class MessagesController {
    @Autowired
    private final @NonNull MessageService messageService;

    @PostMapping(path = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<MessageDto>> postMessage(@RequestBody @Valid MessageDto body) {
        Mono<MessageDto> message = messageService
                .createMessage(MessageDto.toEntity(body))
                .map(MessageDto::fromEntity);
        return new ResponseEntity<Mono<MessageDto>>(message, HttpStatus.CREATED);
    }

    @GetMapping(path = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Flux<MessageDto>>> getMessages() {
        Flux<Message> messages = messageService.getMessages();
        return messages.collectList().flatMap(elements -> {
            if (!elements.isEmpty()) {
                Flux<MessageDto> messageResults = Flux.fromStream(elements.stream()).map(MessageDto::fromEntity);
                return Mono.just(ResponseEntity.ok(messageResults));
            }
            return Mono.just(ResponseEntity.noContent().build());
        });
    }

    @GetMapping(path = "/messages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MessageDto>> getMessage(@PathVariable String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        Mono<Message> message = messageService.getMessage(id);
        return message.flatMap(m -> {
            return Mono.just(ResponseEntity.ok(MessageDto.fromEntity(m)));
        }).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping(path = "/messages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<MessageDto>> putMessage(@PathVariable String id, @RequestBody MessageDto body) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            Set<ConstraintViolation<?>> violations = Collections.<ConstraintViolation<?>>emptySet();
            throw new ConstraintViolationException("ID in URL is not a valid UUID", violations);
        }

        Mono<Message> existingMessage = messageService.getMessage(id);
        Mono<Message> updatedMessage = existingMessage.hasElement().flatMap(
                hasElement -> hasElement
                        ? Mono.defer(() -> messageService.updateMessage(id, MessageDto.toEntity(body)))
                        : Mono.defer(() -> messageService.createMessage(MessageDto.toEntity(body))));

        Mono<MessageDto> message = updatedMessage.map(MessageDto::fromEntity).switchIfEmpty(Mono.empty());
        return new ResponseEntity<Mono<MessageDto>>(message, HttpStatus.OK);
    }

    @DeleteMapping(path = "/messages/{id}")
    public Mono<ResponseEntity<Void>> deleteMessage(@PathVariable String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            return Mono.just(ResponseEntity.accepted().build());
        }

        return messageService.deleteMessage(id).then(Mono.just(ResponseEntity.accepted().build()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
