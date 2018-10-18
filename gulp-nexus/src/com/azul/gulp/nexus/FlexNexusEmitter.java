package com.azul.gulp.nexus;

public interface FlexNexusEmitter {
  public abstract <T> void fire(final Class<T> type, final T value);
}
