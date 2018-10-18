package com.azul.gulp;

public class ProcessingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ProcessingException(final Throwable cause) {
    super(cause);
  }
  
  public ProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
