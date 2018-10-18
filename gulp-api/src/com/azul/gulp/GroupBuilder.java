package com.azul.gulp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GroupBuilder<K, E> implements ResultProvider<Groups<K, E>> {
  private final HashMap<K, GroupImpl<K, E>> groups = new HashMap<>();

  private final Normalizer<K> keyNormalizer;
  
  public GroupBuilder() {
    this(key -> key);
  }
  
  public GroupBuilder(final Normalizer<K> keyNormalizer) {
    this.keyNormalizer = keyNormalizer;
  }
  
  public void add(final K rawKey, final E element) {
    this.getOrCreateGroup(rawKey).add(element);
  }
  
  @Override
  public final Groups<K, E> result() {
    return new GroupsImpl<>(this.keyNormalizer, this.groups);
  }
  
  private final GroupImpl<K, E> getOrCreateGroup(final K rawKey) {
    K normalizedKey;
    try {
      normalizedKey = this.keyNormalizer.normalize(rawKey);
    } catch ( Exception e ) {
      throw new IllegalStateException(e);
    }
    
    return this.groups.computeIfAbsent(normalizedKey, GroupImpl<K, E>::new);
  }
  
  protected static final class GroupsImpl<K, E> implements Groups<K, E> {
    private final Normalizer<K> keyNormalizer;
    private final Map<K, GroupImpl<K, E>> groupsMap;
    
    public GroupsImpl(
      final Normalizer<K> keyNormalizer,
      final Map<K, GroupImpl<K, E>> groupsMap)
    {
      this.keyNormalizer = keyNormalizer;
      this.groupsMap = groupsMap;
    }
    
    @Override
    public final Group<K, E> get(K rawKey) {
      K normalizedKey;
      try {
        normalizedKey = this.keyNormalizer.normalize(rawKey);
      } catch ( Exception e ) {
        throw new IllegalStateException(e);
      }
      
      return this.groupsMap.get(normalizedKey);
    }
    
    @Override
    public final Iterator<Group<K, E>> iterator() {
      return Collections.<Group<K, E>>unmodifiableCollection(this.groupsMap.values()).iterator();
    }
  }
  
  protected static final class GroupImpl<K, E> implements Group<K, E> {
    private final K key;
    private final List<E> elements = new ArrayList<E>();
    
    public GroupImpl(K key) {
      this.key = key;
    }
    
    @Override
    public final K key() {
      return this.key;
    }
    
    @Override
    public final int size() {
      return this.elements.size();
    }
    
    protected void add(final E element) {
      this.elements.add(element);
    }
    
    @Override
    public final Iterator<E> iterator() {
      return Collections.unmodifiableList(this.elements).iterator();
    }
  }
}
