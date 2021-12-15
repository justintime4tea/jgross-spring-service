package tech.jgross.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RSockMessage {
  private String userId;
  private String message;
  private long index;
  private long created = Instant.now().getEpochSecond();

  public RSockMessage(String userId, String message) {
    this.userId = userId;
    this.message = message;
    this.index = 0;
  }

  public RSockMessage(String userId, String message, long index) {
    this.userId = userId;
    this.message = message;
    this.index = index;
  }
}