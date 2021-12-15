package tech.jgross.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMessagesRequest {
  private long offset;
  private long created = Instant.now().getEpochSecond();
}