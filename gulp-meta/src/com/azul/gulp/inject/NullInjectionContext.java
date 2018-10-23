package com.azul.gulp.inject;

import java.lang.reflect.Constructor;

public class NullInjectionContext implements InjectionContext {
  public static final InjectionContext INSTANCE = new NullInjectionContext();
  
  public <T> T get(final Class<T> type) {
    return null;
  }
  
  @Override
  public final <T> T create(final Class<T> type) {
    Constructor<T> ctor;
    try {
      ctor = type.getDeclaredConstructor();
    } catch ( NoSuchMethodException | SecurityException e ) {
      throw new IllegalStateException(e);
    }
    ctor.setAccessible(true);
    try {
      return ctor.newInstance();
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  @Override
  public <T> T get(Class<T> baseType, Class<?>... typeParams) {
    return null;
  }
  
  @Override
  public final void inject(final Object obj) {}
}
