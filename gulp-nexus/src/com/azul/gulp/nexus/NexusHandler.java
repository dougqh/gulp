package com.azul.gulp.nexus;

public interface NexusHandler<T> extends NexusConfigurable {
  public abstract void handle(final T value) throws Exception;
  
  public default void finish() throws Exception {}
}
