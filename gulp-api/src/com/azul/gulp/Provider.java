package com.azul.gulp;

@FunctionalInterface
public interface Provider<T> {
  public abstract T get();
}
