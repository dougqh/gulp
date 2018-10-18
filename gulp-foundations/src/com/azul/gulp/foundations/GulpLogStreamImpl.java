package com.azul.gulp.foundations;

import com.azul.gulp.GulpLogStream;

public final class GulpLogStreamImpl<T>
  extends GulpStreamBase<GulpLogStream<T>, T>
  implements GulpLogStream<T>
{
  public GulpLogStreamImpl(final StreamCore<? extends T> core) {
    super(core);
  }
  
  @Override
  protected GulpLogStream<T> createOffspring(StreamCore<? extends T> core) {
    return new GulpLogStreamImpl<T>(core);
  }
}
