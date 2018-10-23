package com.azul.gulp.inject;

import java.util.Set;

public interface Injector {
  public abstract Set<Class<?>> requires();
  
  public abstract <T> void inject(final Class<T> type, final T value);
}
