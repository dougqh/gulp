package com.azul.gulp.tables;

import com.azul.gulp.Normalizer;

public abstract class GulpRowNormalizer implements Normalizer<GulpRow> {
  public static final GulpRow row(final Object... data) {
    return new GulpRowImpl(data);
  }
}
