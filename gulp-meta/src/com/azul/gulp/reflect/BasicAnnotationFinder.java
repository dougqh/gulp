package com.azul.gulp.reflect;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BasicAnnotationFinder implements AnnotationFinder {
  public static final AnnotationFinder INSTANCE = new BasicAnnotationFinder();	

  private BasicAnnotationFinder() {}
  
  public final <A extends Annotation> List<A> findAnnotationsFor(Class<?> klass, Class<A> annoKlass) {
	return toList(klass.getAnnotationsByType(annoKlass));
  }
  
  private static final <A> List<A> toList(A[] annos) {
	if ( annos.length == 0 ) {
	  return Collections.emptyList();
	} else {
      return Arrays.asList(annos);
	}
  }
}
