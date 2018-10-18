package com.azul.gulp.matching;

public class Patterns {
  public static final PatternBuilder builder() {
    return new PatternBuilder();
  }
  
  public static final Pattern one(final Class<?> type) {
    return builder().one(type).make();
  }
  
  public static final Pattern many(final Class<?> type) {
    return builder().many(type).make();
  }
}
