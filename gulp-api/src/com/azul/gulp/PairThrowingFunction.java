package com.azul.gulp;

@FunctionalInterface
public interface PairThrowingFunction<F, S, R> {
  public R apply(F first, S second);
}
