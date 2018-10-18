package com.azul.gulp.inject;

public interface InjectionContext {
  public abstract <T> T create(final Class<T> type);
  
  // public abstract <T> T create(final Class<T> rawType, final Class<?>... typeParams);
  
  public abstract <T> T get(final Class<T> type);
  
  public abstract <T> T get(final Class<T> rawType, final Class<?>... typeParams);
  
  public abstract void inject(final Object obj);
}
