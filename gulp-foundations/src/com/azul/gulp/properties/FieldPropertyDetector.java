package com.azul.gulp.properties;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class FieldPropertyDetector implements PropertyDetector {
  @Override
  public final Map<String, Property> getProperties(final Class<?> aClass) {
    Set<Field> fields = new HashSet<>();
    Map<String, Property> properties = new HashMap<>();
    
    for ( Field field: aClass.getFields() ) {
      if ( !fields.add(field) ) continue;
      
      field.setAccessible(true);
      properties.put(field.getName(), new FieldProperty(field));
    }
    
    for ( Field field: aClass.getDeclaredFields() ) {
      if ( !fields.add(field) ) continue;
      
      field.setAccessible(true);
      properties.put(field.getName(), new FieldProperty(field));
    }
    
    return Collections.unmodifiableMap(properties);
  }
}
