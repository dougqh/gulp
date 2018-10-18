package com.azul.gulp.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface Property {
  public String name();
  
  public Class<?> targetType();
  
  public Class<?> type();
  
  public Type genericType();
  
  public Object get(Object target);
  
  public <T extends Annotation> T annotation(final Class<T> annoClass);
}
