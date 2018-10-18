package com.azul.gulp;

@FunctionalInterface
public interface PairProcessor<F, S> {
  public void process(F first, S second) throws Exception;
}
