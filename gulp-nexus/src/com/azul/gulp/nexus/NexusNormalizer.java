package com.azul.gulp.nexus;

public interface NexusNormalizer<T> extends NexusConfigurable {
  public abstract T normalize(final T value) throws Exception;
}
