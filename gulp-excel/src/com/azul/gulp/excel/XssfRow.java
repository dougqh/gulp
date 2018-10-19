package com.azul.gulp.excel;

import java.util.List;

import com.azul.gulp.tables.GulpRow;

final class XssfRow extends GulpRow {
  private final List<Object> rowContents;
  
  XssfRow(final List<Object> rowContents) {
    this.rowContents = rowContents;
  }
  
  @Override
  public final <T> T get(int cellIndex) {
    @SuppressWarnings("unchecked")
    T casted = (T)this.rowContents.get(cellIndex);
    return casted;
  }
  
  @Override
  public final String getString(final int cellIndex) {
    Object obj = this.rowContents.get(cellIndex);
    if ( obj == null ) {
      return null;
    } else if ( obj instanceof String ) {
      return (String)obj;
    } else if ( obj instanceof Double ) {
      return obj.toString();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public Boolean getBoolean(int cellIndex) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public final Double getDouble(final int cellIndex) {
    Object obj = this.rowContents.get(cellIndex);
    if ( obj == null ) {
      return null;
    } else if ( obj instanceof String ) {
      return Double.valueOf((String)obj);
    } else if ( obj instanceof Double ) {
      return (Double)obj;
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final Float getFloat(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return ( num == null ) ? null : num.floatValue();
  }
  
  @Override
  public final Integer getInteger(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return ( num == null ) ? null : num.intValue();
  }

  @Override
  public final Long getLong(final int cellIndex) {
    Double num = this.getDouble(cellIndex);
    return ( num == null ) ? null : num.longValue();
  }
}
