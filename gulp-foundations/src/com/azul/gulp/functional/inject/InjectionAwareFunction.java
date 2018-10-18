package com.azul.gulp.functional.inject;

import com.azul.gulp.ThrowingFunction;
import com.azul.gulp.inject.InjectionAware;

public interface InjectionAwareFunction<I, O> 
  extends ThrowingFunction<I, O>, InjectionAware
{
}
