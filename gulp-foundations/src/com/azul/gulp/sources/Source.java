package com.azul.gulp.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azul.gulp.LogProcessingException;
import com.azul.gulp.Processor;
import com.azul.gulp.nexus.Nexus;

public abstract class Source<T> {
  volatile List<T> prefetched = null;
  
  public abstract Class<T> coreType();
  
  public final void forEach(final Processor<? super T> processor) throws Exception {
    List<T> prefetched = this.prefetched;
    if ( prefetched != null ) {
      for ( T prefetchedElement: prefetched ) {
        try {
          processor.process(prefetchedElement);
        } catch ( Exception e ) {
          throw new LogProcessingException(e);
        }
      }
    } else {
      this.forEachImpl(processor);
    }
  }
  
  protected abstract void forEachImpl(final Processor<? super T> processor) throws Exception;
  
  public abstract <V> Converter<T, V> converterFor(final Nexus nexus, final Class<V> type);
  
  protected void prefetch() throws Exception {
    List<T> prefetched = this.prefetched;
    if ( prefetched == null ) {
      int DEFAULT_CAPACITY = 1_000;
      ArrayList<T> loading = new ArrayList<>(DEFAULT_CAPACITY);
      this.forEachImpl(loading::add);
      
      if ( loading.size() < DEFAULT_CAPACITY / 2 ) {
        loading.trimToSize();
      }
      this.prefetched = Collections.unmodifiableList(loading);
    }
  }
}
