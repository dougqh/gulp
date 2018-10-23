package com.azul.gulp.inject;

public interface InjectionAware {
  public abstract void onInject(final InjectionContext ctx);
}
