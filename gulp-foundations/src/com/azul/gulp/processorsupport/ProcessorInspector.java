package com.azul.gulp.processorsupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class ProcessorInspector<A extends Annotation> {
  public static final <A extends Annotation> ProcessorInspector<A> make(final Class<A> annoClass) {
    return new ProcessorInspector<A>() {
      @Override
      protected Class<A> annotationClass() {
        return annoClass;
      }
    };
  }
  
  protected abstract Class<A> annotationClass();
  
  public final List<ProcessorMethod> methods(final Object obj) {
    List<ProcessorMethod> processorMethods = 
      new ArrayList<>(obj.getClass().getMethods().length);
    
    Class<A> annoClass = this.annotationClass();
    
    for ( Method method: obj.getClass().getMethods() ) {
      A anno = method.getAnnotation(annoClass);
      if ( anno == null ) continue;
      
      // Still need to set accessible in case the class is an 
      // anonymous class or a non-public inner class
      method.setAccessible(true);
      
      processorMethods.add(new ProcessorMethod(obj, method));
    }
    
    for ( Method method: obj.getClass().getDeclaredMethods() ) {
      A anno = method.getAnnotation(annoClass);
      if ( anno == null ) continue;
      
      // public methods handled above
      if ( (method.getModifiers() & Modifier.PUBLIC) != 0 ) continue;
      
      method.setAccessible(true);
      
      processorMethods.add(new ProcessorMethod(obj, method));
    }
    
    return processorMethods;
  }
}
