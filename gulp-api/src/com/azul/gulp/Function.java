package com.azul.gulp;

@FunctionalInterface
public interface Function<I, O> extends ThrowingFunction<I, O> {
  public O apply(I in);
}
