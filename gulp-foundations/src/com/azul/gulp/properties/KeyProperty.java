package com.azul.gulp.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.azul.gulp.Gulp;

public final class KeyProperty implements Property {
  private final Property property;
  private final Class<?> keyType;
  
  public KeyProperty(final Property property, final Gulp.Key keyAnno) {
    this.property = property;
    
    if ( keyAnno.value().equals(Object.class) ) {
      this.keyType = property.targetType();
    } else {
      this.keyType = keyAnno.value();
    }
  }
  
  public KeyProperty(final Property property, final Gulp.ForeignKey foreignKeyAnno) {
    this.property = property;
    this.keyType = foreignKeyAnno.value();
  }
  
  @Override
  public final String name() {
    return this.property.name();
  }
  
  @Override
  public final Class<?> targetType() {
    return this.property.targetType();
  }
  
  @Override
  public final Class<?> type() {
    return this.property.type();
  }
  
  @Override
  public final Type genericType() {
    return this.property.genericType();
  }
  
  @Override
  public final <T extends Annotation> T annotation(final Class<T> annoClass) {
    return this.property.annotation(annoClass);
  }
  
  @Override
  public final Object get(final Object target) {
    return this.property.get(target);
  }
  
  public final Class<?> keyType() {
    return this.keyType;
  }
}
