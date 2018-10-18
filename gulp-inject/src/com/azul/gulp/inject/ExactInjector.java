package com.azul.gulp.inject;

import java.lang.reflect.Type;
import java.util.Set;

public interface ExactInjector {
  public abstract Set<Type> requires();
  
  public abstract void inject(final Type type, final Object value);
}
