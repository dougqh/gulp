package com.azul.gulp.text.support;

import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

import com.azul.gulp.text.Line;
import com.azul.gulp.text.RegexMatcher;


public final class AnnotatedRegexLineMatcher<T> extends RegexLineMatcher<T> {
  private final Class<T> dataClass;
  private Constructor<T> cachedCtor;
  
  public AnnotatedRegexLineMatcher(final Class<T> dataClass, final String regex) {
    this(dataClass, Pattern.compile(regex));
  }
  
  AnnotatedRegexLineMatcher(final Class<T> dataClass, final Pattern regex) {
    super(regex);
    this.dataClass = dataClass;
  }
  
  @Override
  public final T fromMatch(Line line, final RegexMatcher matcher) throws Exception {
    Constructor<T> ctor = this.findConstructorFor(matcher);
    Class<?>[] paramTypes = ctor.getParameterTypes();
    
    Object[] params = new Object[paramTypes.length];
    
    try {
      int capturePos = 1;
      for ( int i = 0; i < paramTypes.length; ++i ) {
        params[i] = matcher.groupAs(capturePos++, paramTypes[i]);
      }
    } catch ( Exception e ) {
      throw new IllegalArgumentException(
        "Problem with pattern for " + this.dataClass.getSimpleName() + " @ line " + line.num, e);
    }
    
    try {
      return ctor.newInstance(params);
    } catch ( IllegalAccessException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  private final Constructor<T> findConstructorFor(final RegexMatcher matcher) {
    // caching works on the assumption that the pattern (and therefore the match count)
    // is effectively static.
    if ( this.cachedCtor != null ) {
      return this.cachedCtor;
    }
    
    int paramCount = matcher.groupCount();
    for ( Constructor<?> ctor: this.dataClass.getDeclaredConstructors() ) {
      if ( getAdjustedParamCount(ctor) == paramCount ) {
        @SuppressWarnings("unchecked")
        Constructor<T> castedCtor = (Constructor<T>)ctor;
        castedCtor.setAccessible(true);
        
        this.cachedCtor = castedCtor;
        return castedCtor;
      }
    }
    
    throw new IllegalStateException("No matching constructor for regex");
  }
  
  private static final int getAdjustedParamCount(final Constructor<?> ctor) {
    int count = 0;    
    for ( Class<?> paramType: ctor.getParameterTypes() ) {
      if ( !paramType.equals(Line.class) ) count += 1;
    }
    return count;
  }
}
