package com.azul.gulp.standardplugins;

import java.util.List;

import com.azul.gulp.Gulp;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.Plugin;
import com.azul.gulp.processorsupport.ProcessorInspector;
import com.azul.gulp.processorsupport.ProcessorMethod;

public final class UnhandledPlugin extends Plugin {
  @Override
  public <V> boolean connect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    ProcessorInspector<Gulp.ProcessUnhandled> inspector = 
      new ProcessorInspector<Gulp.ProcessUnhandled>() {
        @Override
        protected Class<Gulp.ProcessUnhandled> annotationClass() {
          return Gulp.ProcessUnhandled.class;
        }
      };
      
    List<ProcessorMethod> methods = inspector.methods(object);
    if ( methods.isEmpty() ) return false;

    for ( final ProcessorMethod method: methods ) {
      Class<?> type = (Class<?>)method.singleArgumentType();
      
      engine.unhandle(type, method::invoke);
    }
    
    return true;
  }
}
