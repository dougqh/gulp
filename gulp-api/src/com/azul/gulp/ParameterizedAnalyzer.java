package com.azul.gulp;

@Deprecated
public interface ParameterizedAnalyzer<I, K, V> 
  extends Processor<I>, ParameterizedResultProvider<K, V>
{
}
