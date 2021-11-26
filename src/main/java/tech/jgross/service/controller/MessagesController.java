package tech.jgross.service.controller;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.jgross.service.dto.MessageDto;
import tech.jgross.service.entity.Message;
import tech.jgross.service.services.MessageService;


@RestController
public class MessagesController {
    @Autowired
    MessageService messageService;

    @GetMapping(path = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<List<MessageDto>>> getMessages() {
        messageService.createMessage(new Message(UUID.randomUUID(), "sender", "message"));
        List<Message> messages = messageService.getMessages();
        return completedFuture(
                ResponseEntity.ok(messages.stream().map(MessageDto::fromEntity).collect(Collectors.toList())));
    }
}
