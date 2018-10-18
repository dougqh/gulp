package com.azul.gulp;

@FunctionalInterface
public interface Normalizer<T> {
  public abstract T normalize(final T value) throws Exception;
}
