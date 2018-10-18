package com.azul.gulp;

import java.lang.reflect.Field;
import java.util.Objects;

public interface ResultProvider<T> extends Printable {
  default T result() {
    Field resultField = Helper.getResultField(this.getClass());
    
    try {
      @SuppressWarnings("unchecked")
      T casted = (T)resultField.get(this);
      return casted;
    } catch ( IllegalArgumentException | IllegalAccessException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  @Override
  default String toPrintString() {
    return Objects.toString(this.result());
  }
  
  static class Helper {
    private static final Field getResultField(final Class<?> aClass) {
      Field resultField = null;
      for ( Field field: aClass.getDeclaredFields() ) {
        if ( field.isAnnotationPresent(Gulp.Result.class) ) {
          if ( resultField != null ) throw new IllegalStateException();
          
          resultField = field;
        }
      }
      if ( resultField == null ) {
        throw new IllegalStateException("result not overridden or annotated: " + aClass);
      }
      
      resultField.setAccessible(true);
      return resultField;
    }
  }
}
