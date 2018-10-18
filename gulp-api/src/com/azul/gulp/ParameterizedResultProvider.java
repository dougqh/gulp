package com.azul.gulp;

@Deprecated
public interface ParameterizedResultProvider<P, V> {
  public abstract V get(P param);
}
