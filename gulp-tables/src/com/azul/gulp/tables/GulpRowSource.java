package com.azul.gulp.tables;

import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.sources.Converter;
import com.azul.gulp.sources.Source;

public abstract class GulpRowSource extends Source<GulpRow> {
  @Override
  public final Class<GulpRow> coreType() {
    return GulpRow.class;
  }
  
  @Override
  public final <V> Converter<GulpRow, V> converterFor(
	final Nexus nexus,
	final Class<V> type)
  {
    return new GulpRowConverter<V>(type);
  }
}
