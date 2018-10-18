package com.azul.gulp.matching;

import java.util.List;

public interface Match {
  public abstract <T> T get(final int index);
  
  public abstract <T> List<T> list(final int index);
}
