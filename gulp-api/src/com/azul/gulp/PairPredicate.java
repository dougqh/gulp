package com.azul.gulp;

@FunctionalInterface
public interface PairPredicate<F, S> {
  public boolean matches(final F first, final S second);
}
