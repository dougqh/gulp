package com.azul.gulp.excel;

import java.io.File;
import java.io.FileInputStream;

import com.azul.gulp.tables.GulpWorkbook;

public final class GulpExcel {
  private GulpExcel() {}
  
  public static final GulpWorkbook gulpFile(final String fileName) {
    return gulpFile(new File(fileName));
  }
  
  public static final GulpWorkbook gulpFile(final File file) {
    if ( file.getName().endsWith(".xlsx") ) {
      return gulpXlsx(file);
    } else if ( file.getName().endsWith(".xls") ) {
      throw new UnsupportedOperationException("No support for older Excel files");
    } else {
      throw new IllegalArgumentException("Improper file type");
    }
  }
  
  public static final GulpWorkbook gulpXlsx(final String fileName) {
    return gulpXlsx(new File(fileName));
  }
  
  public static final GulpWorkbook gulpXlsx(final File file) {
    return new XlsxWorkbook(() -> new FileInputStream(file));
  }
}
