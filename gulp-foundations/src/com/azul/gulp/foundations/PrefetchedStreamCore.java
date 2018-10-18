package com.azul.gulp.foundations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azul.gulp.Processor;
import com.azul.gulp.StreamProcessingException;

final class PrefetchedStreamCore<T> extends StreamCore<T> {
  private final StreamCore<T> wrapped;
  private final List<T> preloaded;
  
  PrefetchedStreamCore(final StreamCore<T> wrapped) {
    this.wrapped = wrapped;
    this.preloaded = preloadFrom(wrapped);
  }
  
  private static final <T> List<T> preloadFrom(final StreamCore<T> wrapped) {
    int DEFAULT_CAPACITY = 1_000;
    ArrayList<T> loading = new ArrayList<>(DEFAULT_CAPACITY);
    wrapped.processImpl(loading::add);
    
    if ( loading.size() < DEFAULT_CAPACITY / 2 ) {
      loading.trimToSize();
    }
    return Collections.unmodifiableList(loading);
  }
  
  @Override
  protected void processImpl(Processor<? super T> processor) throws StreamProcessingException {
    for ( T preloadedElement: this.preloaded ) {
      try {
        processor.process(preloadedElement);
      } catch ( StreamProcessingException e ) {
        throw e;
      } catch ( Exception e ) {
        throw new StreamProcessingException(e);
      }
    }
  }
}
