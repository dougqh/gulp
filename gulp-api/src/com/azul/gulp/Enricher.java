package com.azul.gulp;

@FunctionalInterface
public interface Enricher<T, V> {
  public abstract V enrichment(final T element);
}
