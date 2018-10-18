package com.azul.gulp;

import java.lang.reflect.ParameterizedType;

@Deprecated
public abstract class PredicatedClass<T> implements Predicate<T> {
  @SuppressWarnings("unchecked")
  public final Class<T> coreClass() {
    ParameterizedType paramType = (ParameterizedType)this.getClass().getGenericSuperclass();
    return (Class<T>)paramType.getActualTypeArguments()[0];
  }
}
