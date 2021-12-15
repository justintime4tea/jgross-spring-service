package tech.jgross.service.controller.advice;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Violation {
  private final String fieldName;
  private final String message;
}