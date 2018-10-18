package com.azul.gulp.sources;

import com.azul.gulp.Emitter;
import com.azul.gulp.nexus.Nexus;

public abstract class Converter<I, O> {
  public void init(final Nexus engine) {}
    
  public abstract void convert(
    final I input,
    final Emitter<O> emitter)
    throws Exception;
}
