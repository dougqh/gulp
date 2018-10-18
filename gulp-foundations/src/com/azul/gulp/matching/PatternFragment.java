package com.azul.gulp.matching;

public final class PatternFragment {
  private final Class<?> type;
  private final boolean isMany;
  
  public PatternFragment(final Class<?> type, final boolean isMany) {
    this.type = type;
    this.isMany = isMany;
  }
  
  public final Class<?> type() {
    return this.type;
  }
  
  public final boolean isMany() {
    return this.isMany;
  }
}
