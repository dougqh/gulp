package com.azul.gulp.tables;

import java.lang.reflect.Constructor;

import com.azul.gulp.Emitter;
import com.azul.gulp.sources.Converter;

public final class GulpRowConverter<O> extends Converter<GulpRow, O> {
  private final Constructor<O> ctor;
  private final Class<?>[] paramTypes;
  
  public GulpRowConverter(final Class<O> type) {
    this.ctor = getConstructor(type);
    this.paramTypes = this.ctor.getParameterTypes();
  }
  
  private static final <T> Constructor<T> getConstructor(final Class<T> type) {
    @SuppressWarnings("unchecked")
    Constructor<T>[] ctors = (Constructor[])type.getConstructors();
    if ( ctors.length != 1 ) throw new IllegalStateException();
    return ctors[0];
  }
  
  private final O create(final Object[] args) {
    try {
      return this.ctor.newInstance(args);
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  @Override
  public final void convert(final GulpRow row, final Emitter<O> emitter) throws Exception {
    Object[] params = new Object[this.paramTypes.length];
    for ( int i = 0; i < this.paramTypes.length; ++i ) {
      params[i] = row.get(i, this.paramTypes[i]);
    }
    emitter.fire(this.create(params));
  }
}
