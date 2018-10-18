package com.azul.gulp;

public interface Groups<K, E> extends Iterable<Group<K, E>> {
  public abstract Group<K, E> get(K key);
}
