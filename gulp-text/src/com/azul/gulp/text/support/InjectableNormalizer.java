package com.azul.gulp.text.support;

import com.azul.gulp.Normalizer;
import com.azul.gulp.inject.InjectionAware;

public abstract class InjectableNormalizer<T> implements Normalizer<T>, InjectionAware {}
