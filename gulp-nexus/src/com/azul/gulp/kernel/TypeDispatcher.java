package com.azul.gulp.kernel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.azul.gulp.ProcessingException;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusHandler;
import com.azul.gulp.nexus.NexusNormalizer;
import com.azul.gulp.nexus.NexusUnhandler;

final class TypeDispatcher<T> {
  private final ExceptionHandler exceptionHandler;
  private final NexusNormalizers<T> normalizers;
    
  private final Handlers<T> handlers;

  private T inflight;
  private boolean handled;
  
  public TypeDispatcher(final ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    this.normalizers = new NexusNormalizers<T>(exceptionHandler);
    this.handlers = new Handlers<T>(exceptionHandler);
  }
  
  public final void init(final Nexus ctx) {
    this.exceptionHandler.init(ctx);
    
    this.normalizers.init(ctx);
    
    this.handlers.init(ctx);
  }
  
  public final void fire(final Kernel engine, final T event) {
    T normalized = this.normalizers.normalize(event);
    if ( normalized != null ) {
      this.startHandling(normalized);
      try {
        this.handlers.handle(normalized);
      } finally {
        boolean handled = this.endHandling(normalized);
        if ( !handled ) {
          this.handlers.handleUnhandled(normalized);
        }
      }
    }
  }
  
  public final void addNormalizer(final NexusNormalizer<T> normalizer) {
    this.normalizers.add(normalizer);
  }
  
  public final void removeNormalizer(final NexusNormalizer<T> normalizer) {
    this.normalizers.removeHandler(normalizer);
  }
  
  public final void addHandler(final NexusHandler<? super T> handler) {
    this.handlers.addHandler(handler);
  }
  
  public final void removeHandler(final NexusHandler<? super T> handler) {
    this.handlers.removeHandler(handler);
  }
  
  public final boolean hasUnhandler() {
    return this.handlers.hasUnhandler();
  }
  
  public final void addUnhandler(final NexusUnhandler<? super T> unhandler) {
    this.handlers.addUnhandler(unhandler);
  }
  
  public final void removeUnhandler(final NexusUnhandler<? super T> unhandler) {
    this.handlers.removeUnhandler(unhandler);
  }
  
  public final void markHandled(final T item) {
    if ( this.inflight == item ) this.handled = true;
  }
  
  private void startHandling(final T item) {
    this.inflight = item;
    this.handled = false;
  }
  
  private boolean endHandling(final T item) {
    if ( this.inflight != item ) throw new IllegalStateException();
    
    return this.handled;
  }

  public final void finish() throws ProcessingException {
    // DQH - For ugly use of delayed event firing, there 
    // are probably interesting issues with finishing order here.
    
    this.handlers.finish();
    
    this.normalizers.finish();
    
    this.exceptionHandler.finish();
  }
  
  static final class NexusNormalizers<T> {
    private final ExceptionHandler exceptionHandler;
    private final ArrayList<NexusNormalizer<T>> normalizers = new ArrayList<>(4);
    
    private LinkedHashSet<NexusNormalizer<T>> orderedNormalizers = null;
    private int resetCount = 0;
    
    NexusNormalizers(final ExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
    }
    
    void add(final NexusNormalizer<T> normalizer) {
      this.normalizers.add(normalizer);
    }
    
    void removeHandler(final NexusNormalizer<T> normalizer) {
      this.normalizers.remove(normalizer);
    }
    
    void init(final Nexus ctx) throws ProcessingException {
      for ( NexusNormalizer<T> normalizer: this.normalizers ) {
        try {
          normalizer.init(ctx);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    T normalize(final T value) throws ProcessingException {
      // reset normalizer ordered cache periodically
      this.resetCount += 1;
      if ( this.resetCount == 100 ) {
        this.orderedNormalizers = null;
      }
      
      LinkedHashSet<NexusNormalizer<T>> usedNormalizers = new LinkedHashSet<>(this.normalizers.size());
      
      // if cache isn't empty / clear pick-up prior normalizer application order
      LinkedHashSet<NexusNormalizer<T>> orderedNormalizers;
      if ( this.orderedNormalizers != null ) {
        orderedNormalizers = this.orderedNormalizers;
      } else {
        orderedNormalizers = new LinkedHashSet<>(this.normalizers);
      }
      
      T normalizedValue = value;
      T priorValue;
      do {
        priorValue = normalizedValue;
        normalizedValue = priorValue;
        
        for ( NexusNormalizer<T> normalizer: orderedNormalizers ) {
          if ( usedNormalizers.contains(normalizer) ) continue;
          
          try {
            T curInputValue = normalizedValue;
            T curOutputValue = normalizer.normalize(curInputValue);
            if ( curInputValue != curOutputValue && !curInputValue.equals(curOutputValue) ) {
              usedNormalizers.add(normalizer);
            }
            
            if ( curOutputValue == null ) return null;
            
            normalizedValue = curOutputValue;
          } catch ( Throwable t ) {
            this.exceptionHandler.handle(t);
          }
        }
      } while ( !normalizedValue.equals(priorValue) );
      
      // if the order cache is empty -- populate from the ordered usedNormalizers --
      //   make sure to add any remaining normalizers, too.
      if ( this.orderedNormalizers == null ) {
        this.orderedNormalizers = usedNormalizers;
        this.orderedNormalizers.addAll(this.normalizers);
      }
      
      return normalizedValue;
    }
    
    void finish() {}
  }
  
  private static final class Handlers<T> {
    private final ExceptionHandler exceptionHandler;
    private final ArrayList<NexusHandler<? super T>> handlers = new ArrayList<>(4);
    private List<NexusUnhandler<? super T>> unhandlers = null;
    
    private Nexus initContext = null;
    
    Handlers(final ExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
    }
    
    void init(final Nexus ctx) {
      for ( NexusHandler<? super T> handler: this.handlers ) {
        try {
          handler.init(ctx);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void addHandler(final NexusHandler<? super T> handler) {
      boolean modified = this.handlers.add(handler);
      
      if ( modified && this.initContext != null ) {
        try {
          handler.init(this.initContext);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void removeHandler(final NexusHandler<? super T> handler) {
      boolean modified = this.handlers.remove(handler);
      
      if ( modified && this.initContext != null ) {
        try {
          handler.finish();
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void handle(final T value) {
      for ( NexusHandler<? super T> handler: this.handlers ) {
        try {
          handler.handle(value);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    boolean hasUnhandler() {
      return ( this.unhandlers != null ) && !this.unhandlers.isEmpty();
    }
    
    void addUnhandler(final NexusUnhandler<? super T> unhandler) {
      if ( this.unhandlers == null ) this.unhandlers = new ArrayList<>(4);
      
      boolean modified = this.unhandlers.add(unhandler);
      
      if ( modified && this.initContext != null ) {
        try {
          unhandler.init(this.initContext);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void removeUnhandler(final NexusUnhandler<? super T> unhandler) {
      if ( this.unhandlers == null ) return;
      
      boolean modified = this.unhandlers.remove(unhandler);
      
      if ( modified && this.initContext != null ) {
        try {
          unhandler.finish();
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void handleUnhandled(final T value) {
      if ( this.unhandlers == null ) return;
      
      for ( NexusUnhandler<? super T> handler: this.unhandlers ) {
        try {
          handler.unhandle(value);
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
    
    void finish() {
      for ( NexusHandler<? super T> handler: this.handlers ) {
        try {
          handler.finish();
        } catch ( Throwable t ) {
          this.exceptionHandler.handle(t);
        }
      }
    }
  }
}
