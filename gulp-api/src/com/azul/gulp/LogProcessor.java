package com.azul.gulp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface LogProcessor {
  Set<Class<?>> requiredTypes();
  
  Set<Class<?>> optionalTypes();
  
  <T> Processor<? super T> processorFor(final Class<T> klass);
  
  default <T> Processor<? super T> unhandledProcessorFor(final Class<T> klass) {
    return null;
  }
  
  public static interface Provider {
    public abstract LogProcessor get();
  }
  
  // deliberately not final to allow anonymous double-brace configuration
  public static final class Builder implements LogProcessor.Provider {
    private final Map<Class<?>, Processor<?>> processors = new HashMap<>(16);
    private final Map<Class<?>, Processor<?>> unhandledProcessors = new HashMap<>(4);
    private final Set<Class<?>> requiredTypes = new HashSet<>(8);
    private final Set<Class<?>> optionalTypes = new HashSet<>(4);
    
    public final <T> Builder require(Class<T> klass, Processor<? super T> processor) {
      this.processors.put(klass, processor);
      this.requiredTypes.add(klass);
      return this;
    }
    
    public final <T> Builder optional(Class<T> klass, Processor<? super T> processor) {
      this.processors.put(klass, processor);
      this.optionalTypes.add(klass);
      return this;
    }
    
    public final <T> Builder unhandled(Class<T> klass, Processor<? super T> processor) {
      this.unhandledProcessors.put(klass, processor);
      this.requiredTypes.add(klass);
      return this;
    }
    
    public final LogProcessor make() {
      final Map<Class<?>, Processor<?>> processors = 
        Collections.unmodifiableMap(this.processors);
      final Map<Class<?>, Processor<?>> unhandledProcessors = 
        Collections.unmodifiableMap(this.unhandledProcessors);
      final Set<Class<?>> requiredTypes = 
        Collections.unmodifiableSet(this.requiredTypes);
      final Set<Class<?>> optionalTypes = 
        Collections.unmodifiableSet(this.optionalTypes);
      
      return new LogProcessor() {
        @Override
        public Set<Class<?>> requiredTypes() {
          return requiredTypes;
        }

        @Override
        public Set<Class<?>> optionalTypes() {
          return optionalTypes;
        }

        @Override
        public <T> Processor<? super T> processorFor(Class<T> klass) {
          Processor<?> processor = processors.get(klass);
          
          @SuppressWarnings("unchecked")
          Processor<T> castedProcessor = (Processor<T>)processor;
          return castedProcessor;
        }
        
        @Override
        public <T> Processor<? super T> unhandledProcessorFor(Class<T> klass) {
          Processor<?> processor = unhandledProcessors.get(klass);
          
          @SuppressWarnings("unchecked")
          Processor<T> castedProcessor = (Processor<T>)processor;
          return castedProcessor;
        }
      };
    }
    
    public final LogProcessor get() {
      return this.make();
    }
  }
}
