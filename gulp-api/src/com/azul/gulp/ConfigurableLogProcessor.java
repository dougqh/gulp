package com.azul.gulp;

import java.util.Set;

public abstract class ConfigurableLogProcessor implements LogProcessor {
  private final LogProcessor.Builder builder = new LogProcessor.Builder();
  private LogProcessor wrapped = null;
  
  protected final <T> void require(Class<T> klass, Processor<? super T> processor) {
    this.builder.require(klass, processor);    
  }
    
  protected final <T> void optional(Class<T> klass, Processor<? super T> processor) {
    this.builder.optional(klass, processor);
  }
    
  protected final <T> void unhandled(Class<T> klass, Processor<? super T> processor) {
    this.builder.unhandled(klass, processor);
  }
  
  private final LogProcessor wrapped() {
	if ( this.wrapped == null ) {
	  this.wrapped = this.builder.make();
	}
	return this.wrapped;
  }

  @Override
  public final Set<Class<?>> requiredTypes() {
    return this.wrapped.requiredTypes();
  }
	
  @Override
  public final Set<Class<?>> optionalTypes() {
    return this.wrapped.optionalTypes();
  }
	
  @Override
  public <T> Processor<? super T> processorFor(Class<T> klass) {
    return this.wrapped.processorFor(klass);
  }
	
  @Override
  public <T> Processor<? super T> unhandledProcessorFor(final Class<T> klass) {
    return this.wrapped.unhandledProcessorFor(klass);
  }
}
