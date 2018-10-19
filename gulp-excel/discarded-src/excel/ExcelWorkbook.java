package com.azul.gulp.excel;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Workbook;

import com.azul.gulp.tables.GulpSheet;
import com.azul.gulp.tables.GulpWorkbook;

final class ExcelWorkbook implements GulpWorkbook {
  private final Workbook workbook;
  
  ExcelWorkbook(Workbook workbook) {
    this.workbook = workbook;
  }
  
  @Override
  public final GulpSheet curSheet() {
    return this.sheetAt(this.workbook.getActiveSheetIndex());
  }
  
  @Override
  public final GulpSheet sheetAt(final int index) {
    return new ExcelSheet(this.workbook.getSheetAt(index));
  }
  
  @Override
  public final GulpSheet sheetByName(String name) {
    return new ExcelSheet(this.workbook.getSheet(name));
  }
  
  @Override
  public final Iterable<GulpSheet> sheets() {
    return new Iterable<GulpSheet>() {
      @Override
      public final Iterator<GulpSheet> iterator() {
        return ExcelWorkbook.this.sheetIterator();
      }
    };
  }
  
  private final Iterator<GulpSheet> sheetIterator() {
    final Workbook workbook = ExcelWorkbook.this.workbook;
    
    return new Iterator<GulpSheet>() {
      int sheet = 0;
      
      @Override
      public final boolean hasNext() {
        return ( this.sheet < workbook.getNumberOfSheets() );
      }
      
      @Override
      public final GulpSheet next() {
        if ( this.sheet > workbook.getNumberOfSheets() ) {
          throw new NoSuchElementException();
        }
        return new ExcelSheet(workbook.getSheetAt(this.sheet));
      }
    };
  }
}
