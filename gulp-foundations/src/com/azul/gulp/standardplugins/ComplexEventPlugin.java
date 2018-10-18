package com.azul.gulp.standardplugins;

import com.azul.gulp.Gulp;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.Plugin;

public final class ComplexEventPlugin extends Plugin {
  @Override
  public <V> boolean handleEventRequest(
    final Nexus engine,
    final Class<V> requiredType)
    throws Exception
  {
    Gulp.ComplexEvent eventAnno = requiredType.getAnnotation(Gulp.ComplexEvent.class);
    if ( eventAnno == null ) return false;
    
    engine.get(eventAnno.value());
    
    return true;
  }
  
  @Override
  public <T> boolean connect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    return false;
  }
}
