package com.azul.gulp.tables;

public interface GulpWorkbook {
  public abstract GulpSheet sheetByName(final String name);

  public abstract GulpSheet sheetAt(final int index);
  
  public abstract Iterable<GulpSheet> sheets();

  public abstract GulpSheet curSheet();
}
