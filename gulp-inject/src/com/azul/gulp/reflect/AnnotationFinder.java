package com.azul.gulp.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface AnnotationFinder {
  <A extends Annotation> List<A> findFor(Class<?> klass, Class<A> annoKlass);
  
  <A extends Annotation> List<A> findFor(Field field, Class<A> annoKlass);
  
  <A extends Annotation> List<A> findFor(Method method, Class<A> annoKlass);
}
