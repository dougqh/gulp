package com.azul.gulp.text;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;

import com.azul.gulp.text.support.AnnotatedRegexLineMatcher;
import com.azul.gulp.text.support.CompositeLineMatcher;

public final class GulpText {
  private GulpText() {}
  
  @Inherited
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface LineMatchers {
    public abstract LineMatcher[] value();
  }
  
  @Inherited
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Repeatable(LineMatchers.class)
  public @interface LineMatcher {
    public abstract String regex() default "";
    
    public abstract Class<? extends com.azul.gulp.text.LineMatcher> value() 
      default com.azul.gulp.text.LineMatcher.class;
  }
  
  
  
  
  public static final GulpTextLog gulpFile(final String fileName) {
    return gulpFile(new File(fileName));
  }
  
  public static final GulpTextLog gulpFile(final File fileName) {
    return new GulpTextLog(new LineSource(fileName));
  }
  
  
  
  // TODO: lower visibility
  public static final boolean isRegexBased(final GulpText.LineMatcher anno) {
    return !anno.regex().isEmpty();
  }
  
  // TODO: lower visibility
  public static final boolean usesCustomClass(final GulpText.LineMatcher anno) {
    return !anno.value().equals(com.azul.gulp.text.LineMatcher.class);
  }
  
  public static final <T> com.azul.gulp.text.LineMatcher<T> makeMatcherFrom(
    final Class<T> dataClass,
    final GulpText.LineMatchers anno)
  {
    GulpText.LineMatcher[] matcherAnnos = anno.value();
    
    @SuppressWarnings("unchecked")
    com.azul.gulp.text.LineMatcher<T>[] matchers = new com.azul.gulp.text.LineMatcher[matcherAnnos.length];
    for ( int i = 0; i < matcherAnnos.length; ++i ) {
      matchers[i] = makeMatcherFrom(dataClass, matcherAnnos[i]);
    }
    return new CompositeLineMatcher<T>(matchers);
  }
  
  public static final <T> com.azul.gulp.text.LineMatcher<T> makeMatcherFrom(
    final Class<T> dataClass,
    final GulpText.LineMatcher anno)
  {
    if ( !anno.regex().isEmpty() ) {
      return new AnnotatedRegexLineMatcher<T>(dataClass, anno.regex());
    }
    
    Class<? extends com.azul.gulp.text.LineMatcher> lineMatcherClass = anno.value();
    if ( lineMatcherClass.equals(com.azul.gulp.text.LineMatcher.class) ) {
      throw new IllegalStateException("either value or regex should be set");
    }
    
    
    try {
      try {
        Constructor<? extends com.azul.gulp.text.LineMatcher> ctor = lineMatcherClass.getDeclaredConstructor(Class.class);
        ctor.setAccessible(true);
        return ctor.newInstance(dataClass);
      } catch ( NoSuchMethodException e ) {
        Constructor<? extends com.azul.gulp.text.LineMatcher> ctor = lineMatcherClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
      }
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException(e);
    }
  }
}
