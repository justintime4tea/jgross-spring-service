package tech.jgross.service.dto;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import tech.jgross.service.entity.Message;


@Builder
@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class MessageDto {
  public final @Id @Null(groups = OnCreate.class) @NotNull(message = "'id' must not be null", groups = OnUpdate.class) String id;
  public final @NonNull @NotNull(message = "'userId' must not be null") String userId;
  public @NonNull @NotNull(message = "'message' must not be null") String message;

  public static Message toEntity(MessageDto messageDto) {
    return Message.builder().id(messageDto.id).userId(messageDto.userId).message(messageDto.message).build();
  }

  public static MessageDto fromEntity(Message message) {
    return MessageDto.builder().id(message.id()).userId(message.userId()).message(message.message()).build();
  }
}
