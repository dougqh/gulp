package com.azul.gulp.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public final class FieldProperty implements Property {
  private final Field field;
  
  public FieldProperty(final Field field) {
    this.field = field;
  }
  
  @Override
  public final String name() {
    return this.field.getName();
  }
  
  @Override
  public final Class<?> targetType() {
    return this.field.getDeclaringClass();
  }
  
  @Override
  public final Class<?> type() {
    return this.field.getType();
  }
  
  @Override
  public final Type genericType() {
    return this.field.getGenericType();
  }
  
  @Override
  public final Object get(final Object target) {
    try {
      return this.field.get(target);
    } catch ( IllegalArgumentException | IllegalAccessException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  @Override
  public final <T extends Annotation> T annotation(Class<T> annoClass) {
    return this.field.getAnnotation(annoClass);
  }
}
