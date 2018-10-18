package com.azul.gulp.functional.inject;

import com.azul.gulp.Processor;
import com.azul.gulp.inject.InjectionAware;

public interface InjectionAwareProcessor<T> extends Processor<T>, InjectionAware {
}
