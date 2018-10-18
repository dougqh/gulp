package com.azul.gulp.streamscompatibility;

import java.util.function.Consumer;

import com.azul.gulp.Processor;

public class ProcessorConsumer<T> implements Consumer<T> {
  private final Processor<? super T> processor;
  
  public ProcessorConsumer(final Processor<? super T> proc) {
    this.processor = proc;
  }
  
  @Override
  public void accept(T t) {
    try {
      this.processor.process(t);
    } catch (Exception e) {
      throw new GulpWrapperException(e);
    }
  }
}
