package com.azul.gulp.text.support;

import java.util.regex.Pattern;

import com.azul.gulp.Emitter;
import com.azul.gulp.text.Line;
import com.azul.gulp.text.LineMatcher;
import com.azul.gulp.text.RegexMatcher;

public abstract class RegexLineMatcher<T> implements LineMatcher<T> {
  private final Pattern regex;
  private final boolean matchOriginal;
  
  public RegexLineMatcher(final String regex) {
    this(Pattern.compile(regex));
  }
  
  public RegexLineMatcher(final Pattern regex) {
    this(regex, false);
  }
  
  public RegexLineMatcher(
    final Pattern regex,
    final boolean matchOriginal)
  {
    this.regex = regex;
    this.matchOriginal = matchOriginal;
  }
  
  @Override
  public final void process(final Line line, final Emitter<T> emitter)
    throws Exception
  {
    RegexMatcher matcher = line.match(regex);
    if ( !matcher.matches() ) return;
    
    T event = this.fromMatch(line, matcher);
    if ( event != null ) emitter.fire(event);
  }
  
  public abstract T fromMatch(Line line, final RegexMatcher matcher) throws Exception;
}
