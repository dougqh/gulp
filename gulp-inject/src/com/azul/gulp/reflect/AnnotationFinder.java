package com.azul.gulp.reflect;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationFinder {
  <A extends Annotation> List<A> findAnnotationsFor(Class<?> klass, Class<A> annoKlass);
}
