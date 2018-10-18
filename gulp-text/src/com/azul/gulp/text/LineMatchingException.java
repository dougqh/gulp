package com.azul.gulp.text;

@Deprecated
public final class LineMatchingException extends Exception {
  private static final long serialVersionUID = -3535592198961422797L;

  LineMatchingException(final Line line, final Exception e) {
    super(e.getMessage() + " @ line: " + line.num + " " + line.contents, e);
  }
}
