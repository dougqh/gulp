package com.azul.gulp.tables;

public abstract class GulpRow {
  public abstract <T> T get(final int cellIndex);
  
  public final <T> T get(final int cellIndex, final Class<T> primClass) {
    if ( primClass.equals(String.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getString(cellIndex);
      return cast;
    } else if ( primClass.equals(Boolean.class) || primClass.equals(boolean.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getBoolean(cellIndex);
      return cast;
    } else if ( primClass.equals(Integer.class ) || primClass.equals(int.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getInteger(cellIndex);
      return cast;
    } else if ( primClass.equals(Long.class) || primClass.equals(long.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getLong(cellIndex);
      return cast;
    } else if ( primClass.equals(Double.class) || primClass.equals(double.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getDouble(cellIndex);
      return cast;
    } else if ( primClass.equals(Float.class) || primClass.equals(float.class) ) {
      @SuppressWarnings("unchecked")
      T cast = (T)this.getFloat(cellIndex);
      return cast;
    } else {
      throw new IllegalArgumentException("Unsupported type: " + primClass);
    }
  }
  
  public abstract Boolean getBoolean(int cellIndex);
  
  public abstract Integer getInteger(int cellIndex);
  
  public abstract Long getLong(int cellIndex);
  
  public abstract Float getFloat(int cellIndex);
  
  public abstract Double getDouble(int cellIndex);
  
  public abstract String getString(int cellIndex);
}
