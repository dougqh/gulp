package com.azul.gulp;

@FunctionalInterface
public interface GroupingProcessor<K, E> {
  public abstract void process(
    GroupBuilder<K, E> groups,
    E element)
    throws Exception;
}
