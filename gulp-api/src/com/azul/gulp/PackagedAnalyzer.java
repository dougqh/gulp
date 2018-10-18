package com.azul.gulp;

@FunctionalInterface
public interface PackagedAnalyzer<R> extends ResultProvider<R> {
  public abstract R analyze(final GulpLog log);
}
