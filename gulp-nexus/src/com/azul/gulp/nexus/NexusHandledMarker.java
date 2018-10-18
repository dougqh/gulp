package com.azul.gulp.nexus;

public interface NexusHandledMarker<T> extends NexusConfigurable {
  public abstract boolean isActivated();
  
  public abstract void mark(final T value);
}
