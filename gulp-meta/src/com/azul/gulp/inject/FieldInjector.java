package com.azul.gulp.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.azul.gulp.Gulp;

public class FieldInjector implements ExactInjector {
  private final Object target;
  private final List<Field> injectableFields;
  
  public FieldInjector(final Object target) {
    this.target = target;
    this.injectableFields = getInjectableFields(target.getClass());
  }
  
  private static final List<Field> getInjectableFields(final Class<?> type) {
    List<Field> injectableFields = new ArrayList<Field>();
    
    for ( Field field: type.getFields() ) {
      if ( field.isAnnotationPresent(Gulp.Inject.class) ) {
        injectableFields.add(field);
      }
    }
    for ( Field field: type.getDeclaredFields() ) {
      if ( !isPublic(field) && field.isAnnotationPresent(Gulp.Inject.class)) {
        field.setAccessible(true);
        injectableFields.add(field);
      }
    }
    
    return Collections.unmodifiableList(injectableFields);
  }
  
  private static final boolean isPublic(final Field field) {
    return (field.getModifiers() & Modifier.PUBLIC) != 0;
  }
  
  public final Set<Type> requires() {
    Set<Type> types = new HashSet<Type>(this.injectableFields.size());
    for ( Field field: this.injectableFields ) {
      types.add(field.getGenericType());
    }
    return Collections.unmodifiableSet(types);
  }
  
  @Override
  public void inject(final Type type, final Object value) {
    for ( Field field: this.injectableFields ) {
      if ( field.getGenericType().equals(type) ) {
        try {
          field.set(this.target, value);
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
          throw new IllegalStateException(e);
        }
      }
    }
  }
}
