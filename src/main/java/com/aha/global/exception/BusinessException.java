package com.aha.global.exception;

import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    private final Object data;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data=null;
    }

    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

  public BusinessException(ErrorCode errorCode, Object data) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.data = data;
  }

}
