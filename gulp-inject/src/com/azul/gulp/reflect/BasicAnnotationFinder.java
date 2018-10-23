package com.azul.gulp.reflect;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public final class BasicAnnotationFinder implements AnnotationFinder {
  public static final AnnotationFinder INSTANCE = new BasicAnnotationFinder();	

  private BasicAnnotationFinder() {}
  
  public final <A extends Annotation> List<A> findFor(Class<?> klass, Class<A> annoKlass) {
	return toList(klass.getAnnotation(annoKlass));
  }
  
  private static final <A> List<A> toList(A optionalAnno) {
	if ( optionalAnno == null ) {
	  return Collections.emptyList();
	} else {
      return Collections.singletonList(optionalAnno);
	}
  }
}
