package com.azul.gulp.text;

@FunctionalInterface
public interface LineNormalizer {
  public abstract String normalize(final String line) throws Exception;
}
