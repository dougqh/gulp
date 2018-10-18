package com.azul.gulp.kernel;

import com.azul.gulp.ProcessingException;
import com.azul.gulp.nexus.Nexus;



public abstract class ExceptionHandler {
  public void init(final Nexus ctx) {}
  
  public abstract void handle(final Throwable cause) throws ProcessingException;
  
  public void finish() {}
}
