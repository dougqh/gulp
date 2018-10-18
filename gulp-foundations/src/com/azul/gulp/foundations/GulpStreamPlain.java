package com.azul.gulp.foundations;

public final class GulpStreamPlain<T> 
  extends GulpStreamBase<GulpStreamPlain<T>, T>{
  public GulpStreamPlain(final StreamCore<? extends T> core) {
    super(core);
  }
}
