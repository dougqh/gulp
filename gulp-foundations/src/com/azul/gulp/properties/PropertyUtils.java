package com.azul.gulp.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.azul.gulp.Gulp;

public final class PropertyUtils {
  private PropertyUtils() {}
  
  public static final Map<Class<?>, KeyProperty> findKeys(
    final Map<String, Property> props)
  {
    Map<Class<?>, KeyProperty> keyProps = new HashMap<>();
    
    // First do a pass looking for foreign keys
    for ( Map.Entry<String, Property> propEntry: props.entrySet() ) {
      String name = propEntry.getKey();
      Property origProp = propEntry.getValue();
      
      Gulp.ForeignKey keyAnno = origProp.annotation(Gulp.ForeignKey.class);
      if ( keyAnno != null ) {
        KeyProperty keyProp = new KeyProperty(origProp, keyAnno);
        keyProps.put(keyProp.keyType(), keyProp);
      }
    }
    
    // Second do a pass looking for primary keys -- 
    //   primary keys override foreign keys of the same type
    // First do a pass looking for foreign keys
    for ( Map.Entry<String, Property> propEntry: props.entrySet() ) {
      Property origProp = propEntry.getValue();
      
      Gulp.Key keyAnno = origProp.annotation(Gulp.Key.class);
      if ( keyAnno != null ) {
        KeyProperty keyProp = new KeyProperty(origProp, keyAnno);
        keyProps.put(keyProp.keyType(), keyProp);
      }
    }
    
    return Collections.unmodifiableMap(keyProps);
  }
}
