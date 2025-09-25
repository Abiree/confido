package com.confido.api.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;
}
