package com.azul.gulp;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ConfigurationException extends RuntimeException {
  private static final long serialVersionUID = -1365060037740442599L;

  public ConfigurationException(final Type type) {
    super("Unsatisfied requirement: " + type);
  }
  
  public ConfigurationException(final Class<?> baseType, final Class<?>... paramTypes) {
    super("Unsatisfied requirement: " + baseType + " args: " + Arrays.toString(paramTypes));
  }
  
  public ConfigurationException(final Type type, final Throwable cause) {
    super("Problem configuring requirement: " + type, cause);
  }
  
  public ConfigurationException(final String msg) {
    super(msg);
  }
  
  public ConfigurationException(final Throwable cause) {
    super(cause);
  }
}
