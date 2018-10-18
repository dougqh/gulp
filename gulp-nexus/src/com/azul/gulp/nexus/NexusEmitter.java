package com.azul.gulp.nexus;

public interface NexusEmitter<T> {
  public abstract void fire(final T value);
  
  // TODO: Possibly unification of Emitters
  // public abstract void fire(final Class<? extends T> type, final T value);
}
