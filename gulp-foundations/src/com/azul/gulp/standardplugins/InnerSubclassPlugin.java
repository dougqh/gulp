package com.azul.gulp.standardplugins;

import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.Plugin;

public final class InnerSubclassPlugin extends Plugin {
  @Override
  public final <V> boolean handleEventRequest(
    final Nexus engine, 
    final Class<V> requiredType) throws Exception
  {
    boolean found = false;
    
    for ( Class<?> innerClass: requiredType.getClasses() ) {
      if ( !requiredType.equals(innerClass) && requiredType.isAssignableFrom(innerClass) ) {
        engine.require(innerClass);
        
        found = true;
      }
    }
    return found;
  }
}
