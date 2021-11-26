package tech.jgross.service.dto;

import java.util.UUID;

import org.springframework.lang.NonNull;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import tech.jgross.service.entity.Message;


@Builder
@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public class MessageDto {
  public final @NonNull UUID id;
  public final @NonNull String sender;
  public @NonNull String data;

  public static Message toEntity(MessageDto messageDto) {
    return Message.builder().id(messageDto.id).sender(messageDto.sender).data(messageDto.data).build();
  }

  public static MessageDto fromEntity(Message message) {
    return MessageDto.builder().id(message.id()).sender(message.sender()).data(message.data()).build();
  }
}
