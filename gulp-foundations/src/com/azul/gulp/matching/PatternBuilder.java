package com.azul.gulp.matching;

import java.util.ArrayList;

public final class PatternBuilder {
  private final ArrayList<PatternFragment> fragments = new ArrayList<>(4);
  
  public PatternBuilder() {}
  
  public final PatternBuilder one(final Class<?> type) {
    this.fragments.add(new PatternFragment(type, false));
    return this;
  }
  
  public final PatternBuilder many(final Class<?> type) {
    this.fragments.add(new PatternFragment(type, true));
    return this;
  }
  
  public final Pattern make() {
    return new Pattern(this.fragments);
  }
}
