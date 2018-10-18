package com.azul.gulp;

@FunctionalInterface
public interface ThrowingFunction<I, O> {
  public abstract O apply(I input) throws Exception;
}
