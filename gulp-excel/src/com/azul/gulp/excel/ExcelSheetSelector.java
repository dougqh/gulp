package com.azul.gulp.excel;

public interface ExcelSheetSelector {
  public abstract boolean matches(final int index, final String name);
}
