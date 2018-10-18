package com.azul.gulp.matching;

import java.util.Collections;
import java.util.List;

public final class Pattern {
  private final List<PatternFragment> fragments;
  
  Pattern(final List<PatternFragment> fragments) {
    this.fragments = Collections.unmodifiableList(fragments);
  }
  
  public final List<PatternFragment> fragments() {
    return this.fragments;
  }
}
