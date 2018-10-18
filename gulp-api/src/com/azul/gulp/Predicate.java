package com.azul.gulp;

@FunctionalInterface
public interface Predicate<T> {
  public boolean matches(final T value);
}
