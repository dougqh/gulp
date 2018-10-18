package com.azul.gulp.foundations;

import com.azul.gulp.Predicate;
import com.azul.gulp.Processor;
import com.azul.gulp.StreamProcessingException;
import com.azul.gulp.ThrowingFunction;
import com.azul.gulp.functional.Processors;
import com.azul.gulp.inject.InjectionContext;
import com.azul.gulp.inject.NullInjectionContext;

public abstract class StreamCore<T> {
  protected InjectionContext injectionContext() {
    return NullInjectionContext.INSTANCE;
  }
  
  public final void process(final Processor<? super T> processor) {
    this.injectionContext().inject(processor);
    
    this.processImpl(processor);
  }
  
  protected abstract void processImpl(final Processor<? super T> processor)
    throws StreamProcessingException;
  
  public StreamCore<T> filter(final Predicate<? super T> predicate) {
    final StreamCore<T> wrapped = this;
    return new StreamCore<T>() {
      @Override
      protected final InjectionContext injectionContext() {
        return wrapped.injectionContext();
      }
      
      @Override
      protected final void processImpl(final Processor<? super T> processor) {
        wrapped.processImpl(Processors.filter(predicate, processor));
      }
    };
  }
  
  public <U> StreamCore<U> map(final ThrowingFunction<? super T, ? extends U> mappingFn) {
    final StreamCore<T> wrapped = this;
    return new StreamCore<U>() {
      @Override
      protected final InjectionContext injectionContext() {
        return wrapped.injectionContext();
      }
      
      @Override
      public void processImpl(Processor<? super U> processor) {
        wrapped.processImpl(Processors.map(mappingFn, processor));
      }
    };
  }
  
  public <U> StreamCore<U> flatMap(final ThrowingFunction<? super T, ? extends Iterable<? extends U>> mappingFn) {
    final StreamCore<T> wrapped = this;
    return new StreamCore<U>() {
      @Override
      protected final InjectionContext injectionContext() {
        return wrapped.injectionContext();
      }
      
      @Override
      public void processImpl(Processor<? super U> processor) {
        wrapped.processImpl(t -> {
          Iterable<? extends U> iterable = mappingFn.apply(t);
          
          for ( U u: iterable ) {
            processor.process(u);
          }
        });
      }
    };
  }
}
