package com.azul.gulp;

@FunctionalInterface
public interface Emitter<O> {
  public abstract void fire(final O value);
}
