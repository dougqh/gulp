package com.azul.gulp;

@FunctionalInterface
public interface IndexedProcessor<T> {
  public abstract void process(final int index, final T object) throws Exception;
}
