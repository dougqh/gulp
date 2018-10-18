package com.azul.gulp;

@Deprecated
public final class LogProcessingException extends ProcessingException {
  private static final long serialVersionUID = -4403838974277033935L;
  
  public static final LogProcessingException wrap(final Throwable cause) {
    if ( cause instanceof LogProcessingException ) {
      return (LogProcessingException)cause;
    } else {
      return new LogProcessingException(cause);
    }
  }

  public LogProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public LogProcessingException(final Throwable cause) {
    super(cause);
  }
}
