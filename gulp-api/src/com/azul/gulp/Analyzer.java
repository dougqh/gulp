package com.azul.gulp;

public interface Analyzer<T, R> extends Processor<T>, ResultProvider<R> {}
