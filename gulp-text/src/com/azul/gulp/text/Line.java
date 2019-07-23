package com.azul.gulp.text;

import java.util.regex.Pattern;

public final class Line {
  public final int num;
  public final String contents;
  public final String originalContents;
  
  public Line(final int num, final String contents) {
    this(num, contents, contents);
  }
  
  public Line(final int num, final String contents, final String originalContents) {
    this.num = num;
    this.contents = contents;
    this.originalContents = originalContents;
  }
  
  public Line(final Line baseLine, final String contents) {
    this.num = baseLine.num;
    this.originalContents = baseLine.originalContents;
    this.contents = contents;
  }
  
  public final int length() {
    return this.contents.length();
  }
  
  public final char charAt(final int i) {
    return this.contents.charAt(i);
  }
  
  public final String[] split(final String regex) {
    return this.contents.split(regex);
  }
  
  public final String[] split(final String regex, final int limit) {
    return this.contents.split(regex, limit);
  }
  
  public final int indexOf(final char ch) {
    return this.contents.indexOf(ch);
  }
  
  public final int indexOf(final char ch, final int fromIndex) {
    return this.contents.indexOf(ch, fromIndex);
  }
  
  public final int indexOf(final String substr) {
    return this.contents.indexOf(substr);
  }
  
  public final int indexOf(final String substr, final int fromIndex) {
    return this.contents.indexOf(substr, fromIndex);
  }
  
  public final int lastIndexOf(final char ch) {
    return this.contents.lastIndexOf(ch);
  }
  
  public final int lastIndexOf(final char ch, final int fromIndex) {
    return this.contents.lastIndexOf(ch, fromIndex);
  }
  
  public final int lastIndexOf(final String substr) {
    return this.contents.lastIndexOf(substr);
  }
  
  public final int lastIndexOf(final String substr, final int fromIndex) {
    return this.contents.lastIndexOf(substr, fromIndex);
  }
  
  public final String before(final int needle) {
    int index = this.contents.indexOf(needle);
    if ( index == -1 ) {
      return this.contents;
    } else {
      return this.contents.substring(0, index);
    }
  }
  
  public final String substring(final int begin) {
    return this.contents.substring(begin);
  }
  
  public final Line subLine(final int begin) {
    return new Line(this.num, this.substring(begin), this.originalContents);
  }
  
  
  public final String substring(final int begin, final int end) {
    return this.contents.substring(begin, end);
  }
  
  public final Line subLine(final int begin, final int end) {
    return new Line(this.num, this.substring(begin, end), this.originalContents);
  }
  
  public final Line trim() {
    return new Line(this.num, this.contents.trim(), this.originalContents);
  }
  
  public final boolean startsWith(final String value) {
    return this.contents.startsWith(value);
  }
  
  public final boolean endsWith(final String value) {
    return this.contents.endsWith(value);
  }
  
  public final boolean contains(final String value) {
    return this.contents.contains(value);
  }
  
  public final boolean contains(final char ch) {
    for ( int i = 0; i < this.contents.length(); ++i ) {
      if ( this.contents.charAt(i) == ch ) return true;
    }
    return false;
  }
  
  public final Line replace(final String subStr, final String replacementStr) {
    return new Line(this.num, this.contents.replace(subStr, replacementStr), this.originalContents);
  }
  
  public final Line strip(final Pattern regex) {
    return this.replace(regex, "");
  }
  
  // replaces the match group with specified string
  public final Line replace(final Pattern regex, final String replacement) {
    RegexMatcher matcher = match(regex);
    if ( !matcher.matches() ) return this;

    if ( matcher.groupCount() != 1 ) throw new IllegalArgumentException();
    
    int start = matcher.groupStart(1);
    int end = matcher.groupEnd(1);
        
    String prefix = this.contents.substring(0,  start);
    String suffix = this.contents.substring(end);
      
    Line normalizedLine = new Line(this.num, prefix + replacement + suffix, this.originalContents);
    return normalizedLine;
  }
  
  public final RegexMatcher match(final String regex) {
    return this.match(Pattern.compile(regex));
  }
  
  public final RegexMatcher match(final Pattern regex, final boolean matchOriginal) {
    return RegexMatcher.match(regex, matchOriginal ? this.originalContents : this.contents);
  }
  
  public final RegexMatcher match(final Pattern regex) {
    return RegexMatcher.match(regex, this.contents);
  }
  
  public final RegexMatcher matchOriginal(final Pattern regex) {
    return RegexMatcher.match(regex, this.originalContents);
  }
  
  public final boolean equals(final String str) {
    return this.contents.equals(str);
  }
  
  @Override
  public final int hashCode() {
    return this.contents.hashCode();
  }
  
  @Override
  public final boolean equals(final Object obj) {
    if ( obj instanceof String ) {
      String str = (String)obj;
      return this.equals(str);
    }
    
    if ( !(obj instanceof Line) ) return false;
    
    Line that = (Line)obj;
    return (this.num == that.num) && this.contents.equals(that.contents);
  }
  
  public final char[] toCharArray() {
    return this.contents.toCharArray();
  }
  
  @Override
  public final String toString() {
    return String.format("%4d: %s ----- %s", this.num, this.contents, this.originalContents);
  }
}
