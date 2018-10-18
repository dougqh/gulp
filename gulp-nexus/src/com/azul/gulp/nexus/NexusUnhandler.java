package com.azul.gulp.nexus;

public interface NexusUnhandler<T> extends NexusConfigurable {
  public abstract void unhandle(final T value) throws Exception;
  
  public default void finish() throws Exception {}
}
