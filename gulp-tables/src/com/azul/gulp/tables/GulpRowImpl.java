package com.azul.gulp.tables;

final class GulpRowImpl extends GulpRow {
  private final Object[] data;
  
  public GulpRowImpl(final Object... data) {
    this.data = data;
  }
  
  @Override
  public <T> T get(int cellIndex) {
    @SuppressWarnings("unchecked")
    T casted = (T)this.data(cellIndex);
    return casted;
  }
  
  private final Object data(final int cellIndex) {
    return this.data[cellIndex];
  }
  
  @Override
  public final Boolean getBoolean(final int cellIndex) {
    Object data = this.data(cellIndex);
    if ( data == null ) {
      return null;
    } else if ( data instanceof Boolean ) {
      return (Boolean)data;
    } else if ( data instanceof Number ) {
      Number num = (Number)data;
      return ( num.intValue() != 0 );
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final Double getDouble(int cellIndex) {
    Object data = this.data(cellIndex);
    if ( data == null ) {
      return null;
    } else if ( data instanceof Number ) {
      Number num = (Number)data;
      return num.doubleValue();
    } else if ( data instanceof String ) {
      String str = (String)data;
      return Double.parseDouble(str);
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final Float getFloat(int cellIndex) {
    Object data = this.data(cellIndex);
    if ( data == null ) {
      return null;
    } else if ( data instanceof Number ) {
      Number num = (Number)data;
      return num.floatValue();
    } else if ( data instanceof String ) {
      String str = (String)data;
      return Float.parseFloat(str);
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final Integer getInteger(int cellIndex) {
    Object data = this.data(cellIndex);
    if ( data == null ) {
      return null;
    } else if ( data instanceof Number ) {
      Number num = (Number)data;
      return num.intValue();
    } else if ( data instanceof String ) {
      String str = (String)data;
      return Integer.parseInt(str, 10);
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final Long getLong(int cellIndex) {
    Object data = this.data(cellIndex);
    if ( data == null ) {
      return null;
    } else if ( data instanceof Number ) {
      Number num = (Number)data;
      return num.longValue();
    } else if ( data instanceof String ) {
      String str = (String)data;
      return Long.parseLong(str, 10);
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public final String getString(final int cellIndex) {
    Object data = this.data(cellIndex);
    return ( data == null ) ? null : data.toString();
  }
}
