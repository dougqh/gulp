package com.azul.gulp;

public interface Group<K, E> extends Iterable<E> {
  public K key();
  
  public int size();
}
