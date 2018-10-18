package com.azul.gulp.standardplugins;

import java.util.List;

import com.azul.gulp.Gulp;
import com.azul.gulp.matching.Match;
import com.azul.gulp.matching.Pattern;
import com.azul.gulp.matching.PatternHandler;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.Plugin;
import com.azul.gulp.processorsupport.ProcessorInspector;
import com.azul.gulp.processorsupport.ProcessorMethod;

public final class ProcessorPlugin extends Plugin {
  @Override
  public <V> boolean connect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    ProcessorInspector<Gulp.Process> inspector = 
      new ProcessorInspector<Gulp.Process>() {
        @Override
        protected Class<Gulp.Process> annotationClass() {
          return Gulp.Process.class;
        }
      };
      
    List<ProcessorMethod> methods = inspector.methods(object);
    if ( methods.isEmpty() ) return false;

    for ( final ProcessorMethod method: methods ) {
      final Pattern pattern = method.pattern();
      PatternHandler handler = new PatternHandler() {
        @Override
        protected final Pattern pattern() {
          return pattern;
        }
        
        @Override
        protected void match(final Match match) throws Exception {
          method.invoke(match); 
        }
      };
      
      boolean optional = method.anno(Gulp.Process.class).optional();
      if ( optional ) {
        engine.handleOptional(handler);
      } else {
        engine.handle(handler);
      }
    }
    
    return true;
  }
}
