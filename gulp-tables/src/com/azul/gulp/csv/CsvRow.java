package com.azul.gulp.csv;

import org.apache.commons.csv.CSVRecord;

import com.azul.gulp.tables.GulpRow;

public final class CsvRow extends GulpRow {
  private final CSVRecord record;
  
  public CsvRow(final CSVRecord record) {
    this.record = record;
  }
  
  @Override
  public final <T> T get(final int cellIndex) {
    @SuppressWarnings("unchecked")
    T casted = (T)this.getString(cellIndex);
    return casted;
  }
  
  @Override
  public final String getString(final int cellIndex) {
    return ( cellIndex >= this.record.size() ) ? null : this.record.get(cellIndex).trim();
  }
  
  @Override
  public final Boolean getBoolean(final int cellIndex) {
    String value = this.getString(cellIndex);
    if ( value == null ) return null;
    
    switch ( value ) {
      case "0":
      case "false":
      case "False":
      case "FALSE":
      return false;
      
      case "1":
      case "true":
      case "True":
      case "TRUE":
      return true;
      
      default:
      throw new IllegalArgumentException("not a booelan - " + value);
    }
  }
  
  @Override
  public final Integer getInteger(final int cellIndex) {
    String value = this.getString(cellIndex);
    return ( value == null ) ? null : Integer.parseInt(value, 10);
  }
  
  @Override
  public final Long getLong(int cellIndex) {
    String value = this.getString(cellIndex);
    return ( value == null ) ? null : Long.parseLong(value, 10);
  }
  
  @Override
  public final Float getFloat(int cellIndex) {
    String value = this.getString(cellIndex);
    return ( value == null ) ? null : Float.parseFloat(value);
  }
  
  @Override
  public final Double getDouble(int cellIndex) {
    String value = this.getString(cellIndex);
    return ( value == null ) ? null : Double.parseDouble(value);
  }
}
