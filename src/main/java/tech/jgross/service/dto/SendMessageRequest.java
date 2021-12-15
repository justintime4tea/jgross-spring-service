package tech.jgross.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
  private String message;
  private long created = Instant.now().getEpochSecond();
}