package com.azul.gulp;

@FunctionalInterface
public interface Processor<T> {
  public abstract void process(final T object) throws Exception;
}
