package com.azul.gulp.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.azul.gulp.tables.GulpRow;

final class ExcelRow extends GulpRow {
  private final Row row;
  
  ExcelRow(final Row row) {
    this.row = row;
  }
  
  private final Cell getCell(final int cellIndex) {
    return this.row.getCell(cellIndex);
  }
  
  @Override
  public final String getString(final int cellIndex) {
    Cell cell = this.getCell(cellIndex);
    return cell == null ? null : cell.getStringCellValue();
  }
  
  @Override
  public final Boolean getBoolean(final int cellIndex) {
    Cell cell = this.getCell(cellIndex);
    return cell == null ? null : cell.getBooleanCellValue();
  }
  
  @Override
  public final Double getDouble(final int cellIndex) {
    Cell cell = this.getCell(cellIndex);
    return cell == null ? null : cell.getNumericCellValue();
  }
  
  @Override
  public final Float getFloat(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return num == null ? null : num.floatValue();
  }
  
  @Override
  public final Integer getInteger(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return num == null ? null : num.intValue();
  }

  @Override
  public final Long getLong(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return num == null ? null : num.longValue();
  }
}
