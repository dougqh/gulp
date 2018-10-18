package com.azul.gulp.io;

import java.io.IOException;

@FunctionalInterface
public interface IoProvider<T> {
  public abstract T open() throws IOException; 
}