package com.azul.gulp.excel;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.azul.gulp.tables.GulpWorkbook;

public final class GulpExcel {
  private GulpExcel() {}
  
  public static final GulpWorkbook fromFile(final String fileName) {
    return fromFile(fileName);
  }
  
  public static final GulpWorkbook fromFile(final File file) {
    if ( file.getName().endsWith(".xlsx") ) {
      return fromXlsx(file);
    } else if ( file.getName().endsWith(".xls") ) {
      throw new UnsupportedOperationException("No support for older Excel files");
    } else {
      throw new IllegalArgumentException("Improper file type");
    }
  }
  
  public static final GulpWorkbook fromXlsx(final String fileName) {
    return fromXlsx(new File(fileName));
  }
  
  public static final GulpWorkbook fromXlsx(final File file) {
    try {
      return new ExcelWorkbook(new XSSFWorkbook(file));
    } catch ( InvalidFormatException | IOException e ) {
      throw new IllegalStateException(e);
    }
  }
}
