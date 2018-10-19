package com.azul.gulp.csv;

import java.io.File;

public final class GulpCsv {
  private GulpCsv() {}
  
  public static final CsvSheet gulpCsv(final String file) {
    return gulpCsv(new File(file));
  }
  
  public static final CsvSheet gulpCsv(final File file) {
    return new CsvSheet(new CsvRowSource(file));
  }
  
  public static final CsvSheet gulpCsv(final String file, final char delimiter) {
    return gulpCsv(new File(file), delimiter);
  }
  
  public static final CsvSheet gulpCsv(final File file, final char delimiter) {
    return new CsvSheet(new CsvRowSource(file, delimiter));
  }
}
