package com.azul.gulp.text.support;

import java.util.Arrays;
import java.util.List;

import com.azul.gulp.Emitter;
import com.azul.gulp.inject.InjectionAware;
import com.azul.gulp.inject.InjectionContext;
import com.azul.gulp.text.Line;
import com.azul.gulp.text.LineMatcher;

public final class CompositeLineMatcher<T> implements LineMatcher<T>, InjectionAware {
  private final List<? extends LineMatcher<T>> matchers;
  
  @SafeVarargs
  public CompositeLineMatcher(final LineMatcher<T>... matchers) {
    this(Arrays.asList(matchers));
  }
  
  public CompositeLineMatcher(final List<? extends LineMatcher<T>> matchers) {
    this.matchers = matchers;
  }
  
  @Override
  public final void onInject(final InjectionContext ctx) {
    for ( LineMatcher<T> matcher: this.matchers ) {
      ctx.inject(matcher);
    }
  }
  
  @Override
  public final void process(final Line line, final Emitter<T> emitter)
    throws Exception
  {
    final TrackingEmitter<T> trackingEmitter = new TrackingEmitter<T>(emitter);
    final ExceptionHelper exHelper = new ExceptionHelper();
    
    for ( LineMatcher<T> matcher: this.matchers ) {
      try {
        matcher.process(line, emitter);
      } catch ( Exception e ) {
        exHelper.recordException(e);
      }
    }
    
    if ( !trackingEmitter.fired() ) exHelper.rethrow();
  }
  
  static final class TrackingEmitter<T> implements Emitter<T> {
    private final Emitter<T> wrappedEmitter;
    private boolean fired = false;
    
    public TrackingEmitter(final Emitter<T> emitter) {
      this.wrappedEmitter = emitter;
    }
    
    public void reset() {
      this.fired = false;
    }
    
    @Override
    public void fire(T value) {
      this.wrappedEmitter.fire(value);
      this.fired = true;
    }
    
    public boolean fired() {
      return this.fired;
    }
  }
}
