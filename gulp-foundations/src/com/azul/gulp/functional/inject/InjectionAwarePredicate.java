package com.azul.gulp.functional.inject;

import com.azul.gulp.Predicate;
import com.azul.gulp.inject.InjectionAware;

public interface InjectionAwarePredicate<T> extends Predicate<T>, InjectionAware {}
