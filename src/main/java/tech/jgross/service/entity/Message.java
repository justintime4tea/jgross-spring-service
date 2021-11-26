package tech.jgross.service.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
@Entity
@Accessors(fluent = true)
@RequiredArgsConstructor
public class Message implements Serializable {
  @Id
  @Type(type = "uuid-char")
  @Column(unique = true, nullable = false, columnDefinition = "uuid")
  @Builder.Default
  private final @NonNull UUID id = UUID.randomUUID();
  private final @NonNull String sender;
  private @NonNull String data;
}
