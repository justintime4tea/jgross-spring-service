package tech.jgross.service.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@Builder
@Data
@Entity
@Accessors(fluent = true)
@RequiredArgsConstructor
public class Message implements Serializable {
  @Id
  @Type(type = "uuid-char")
  @Column(unique = true, nullable = false, columnDefinition = "uuid")
  private final @NonNull String id;
  private final @NonNull String userId;
  private @NonNull String message;
}
