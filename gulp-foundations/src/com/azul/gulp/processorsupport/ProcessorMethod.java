package com.azul.gulp.processorsupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.azul.gulp.matching.Match;
import com.azul.gulp.matching.Pattern;
import com.azul.gulp.matching.PatternBuilder;

public final class ProcessorMethod {
  private final Object target;
  private final Method method;
  
  public ProcessorMethod(final Object target, final Method method) {
    this.target = target;
    this.method = method;
  }
  
  public <A extends Annotation> A anno(Class<A> annoClass) {
    return this.method.getAnnotation(annoClass);
  }
  
  public final Type singleArgumentType() {
    Type[] types = this.method.getGenericParameterTypes();
    if ( types.length != 1 ) throw new IllegalStateException("expected single argument");
    
    return types[0];
  }
  
  public final Pattern pattern() {
    // TODO: Add support for lists
    PatternBuilder patternBuilder = new PatternBuilder();
    for ( Class<?> type: this.method.getParameterTypes() ) {
      patternBuilder.one(type);
    }
    return patternBuilder.make();
  }
  
  public final void invoke(final Object... args) throws Exception {
    this.method.invoke(this.target, args);
  }
  
  public final void invoke(final Match match) throws Exception {
    Object[] params = new Object[this.method.getParameterTypes().length];
    for ( int i = 0; i < params.length; ++i ) {
      params[i] = match.get(i);
    }
    
    this.method.invoke(this.target, params);
  }
  
  @Override
  public final String toString() {
    return this.method.toString();
  }
}
