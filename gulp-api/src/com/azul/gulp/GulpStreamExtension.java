package com.azul.gulp;

import java.util.Set;

public interface GulpStreamExtension<E extends GulpStreamExtension<E, T>, T> 
  extends GulpStream<T>
{
  public abstract E filter(Predicate<? super T> predicate);
  
  public abstract E filter(Set<? super T> set);
  
  @SuppressWarnings("unchecked")
  public abstract E filter(T... set);
  
  public abstract E filterOut(Predicate<? super T> predicate);
  
  public abstract E filterOut(Set<? super T> set);
  
  @SuppressWarnings("unchecked")
  public abstract E filterOut(T... set);
  
  public abstract E unique();
  
  public abstract E unique(final ThrowingFunction<? super T, ?> identityFn);

}
