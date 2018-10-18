package com.azul.gulp.text;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexMatcher {
  private final Matcher matcher;
  private int nextGroup = 1;

  public static final RegexMatcher match(
    final Pattern pattern,
    final CharSequence corpus)
  {
    return new RegexMatcher(pattern.matcher(corpus));
  }
  
  public RegexMatcher(final Matcher matcher) {
    this.matcher = matcher;
  }
  
  public final boolean matches() {
    return this.matcher.matches();
  }
  
  public final int groupCount() {
    return this.matcher.groupCount();
  }
  
  public final String group(final int index) {
    return this.matcher.group(index);
  }
  
  public final int groupStart(final int index) {
    return this.matcher.start(index);
  }
  
  public final int groupEnd(final int index) {
    return this.matcher.end(index);
  }
  
  public final <T> T groupAs(
      final int index,
      final Class<? extends T> type)
  {
    return convert(this.group(index), type, null);
  }
  
  public final <T> T groupAs(
      final int index,
      final Class<? extends T> type,
      final Object param)
  {
    return convert(this.group(index), type, param);
  }
  
  public final String stringAt(final int index) {
    return this.group(index);
  }
  
  public final Integer intAt(final int index) {
    return this.groupAs(index, Integer.class);
  }
  
  public final Integer intAt(final int index, final int base) {
    return this.groupAs(index, Integer.class, base);
  }
  
  public final Character charAt(final int index) {
    return this.groupAs(index, Character.class);
  }
  
  public final Date dateAt(final int index, final DateFormat format) {
    return this.groupAs(index, Date.class, format);
  }
  
  public final long timeAt(final int index, final DateFormat format) {
    return this.dateAt(index, format).getTime();
  }
  
  public final String nextGroup() {
    return this.matcher.group(this.nextGroup++);
  }
  
  public final Double nextDouble() {
    return this.nextGroupAs(Double.class);
  }
  
  public final Integer nextInt() {
    return this.nextGroupAs(Integer.class);
  }
  
  public final Integer nextInt(final int base) {
    return this.nextGroupAs(Integer.class, base);
  }
  
  public final Long nextLong() {
    return this.nextGroupAs(Long.class);
  }
  
  public final Long nextLong(final int base) {
    return this.nextGroupAs(Long.class, base);
  }
  
  public final Date nextDate(final DateFormat format) {
    return this.nextGroupAs(Date.class, format);
  }
  
  public final long nextTime(final DateFormat format) {
    return this.nextDate(format).getTime();
  }
  
  public final Character nextChar() {
    return this.nextGroupAs(Character.class);
  }
  
  public final String nextString() {
    return this.nextGroup();
  }
  
  public final boolean hasNext() {
    return ( this.nextGroup < this.groupCount() );
  }

  
  public final <T> T nextGroupAs(final Class<? extends T> type) {
    return convert(this.nextGroup(), type, null);
  }
  
  public final <T> T nextGroupAs(
    final Class<? extends T> type,
    final Object param)
  {
    return convert(this.nextGroup(), type, param);
  }
  
  private static final <T> T convert(
      final String value,
      final Class<? extends T> type,
      final Object optionalParam)
  {    
    if ( type.equals(String.class) ) {
      return cast(value);
    }
    if ( type.equals(char.class) || type.equals(Character.class) ) {
      return cast(parseChar(value));
    }
    
    if ( value.isEmpty() ) {
      return null;
    }
    
    if ( type.equals(int.class) || type.equals(Integer.class) ) {
      String normalizedValue = value.replace(",", "");
      
      int base = (optionalParam == null) ? 10 : (Integer)optionalParam;
      return cast(Integer.parseInt(normalizedValue, base));
    }
    
    if ( type.equals(long.class) || type.equals(Long.class) ) {
      String normalizedValue = value.replace(",", "");
      
      int base = (optionalParam == null) ? 10 : (Integer)optionalParam;
      return cast(Long.parseLong(normalizedValue, base));
    }
    
    if ( type.equals(double.class) || type.equals(Double.class) ) {
      String normalizedValue = value.replace(",", "");
      
      return cast(Double.parseDouble(normalizedValue));
    }
    
    if ( type.equals(Date.class) ) {
      DateFormat format = (DateFormat)optionalParam;
      if ( optionalParam == null ) throw new IllegalStateException("Must specify DateFormat");
      
      try {
        return cast(format.parse(value.trim()));
      } catch ( ParseException e ) {
        throw new IllegalArgumentException(e);
      }
    }
    
    throw new UnsupportedOperationException("unsupported type");
  }
  
  private static final char parseChar(final String value) {
    switch ( value.length() ) {
      case 0:
      return '\0';
      
      case 1:
      return value.charAt(0);
      
      default:
      throw new IllegalStateException("Expected single char match");
    }
  }
  
  @SuppressWarnings("unchecked")
  private static final <T> T cast(final Object value) {
    return (T)value;
  }
  
  @Override
  public final String toString() {
    return this.matcher.toString();
  }
}
