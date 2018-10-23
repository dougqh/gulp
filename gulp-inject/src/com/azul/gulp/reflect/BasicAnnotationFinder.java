package com.azul.gulp.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public final class BasicAnnotationFinder implements AnnotationFinder {
  public final <A extends Annotation> List<A> findFor(Class<?> klass, Class<A> annoKlass) {
	return toList(klass.getAnnotation(annoKlass));
  }
  
  public final <A extends Annotation> List<A> findFor(Field field, Class<A> annoKlass) {
	return toList(field.getAnnotation(annoKlass));
  }
  
  public final <A extends Annotation> List<A> findFor(Method method, Class<A> annoKlass) {
	return toList(method.getAnnotation(annoKlass));
  }
  
  private static final <A> List<A> toList(A optionalAnno) {
	if ( optionalAnno == null ) {
	  return Collections.emptyList();
	} else {
      return Collections.singletonList(optionalAnno);
	}
  }
}
