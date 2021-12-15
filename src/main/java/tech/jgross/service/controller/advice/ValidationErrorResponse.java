package tech.jgross.service.controller.advice;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class ValidationErrorResponse {
  private List<Violation> violations = new ArrayList<>();
}