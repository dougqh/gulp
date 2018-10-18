package com.azul.gulp.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azul.gulp.LogProcessingException;
import com.azul.gulp.nexus.FlexNexusEmitter;
import com.azul.gulp.nexus.FlexNexusHandler;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusEmitter;
import com.azul.gulp.nexus.NexusHandledMarker;
import com.azul.gulp.nexus.NexusHandler;
import com.azul.gulp.nexus.NexusNormalizer;
import com.azul.gulp.nexus.NexusUnhandler;

/**
 * Minimal dispatcher does not perform any fancy annotated based look-ups 
 * or reflection magic.  Simply creates a series of type dispatchers 
 * interconnected based on the type hierarchy and provides a way to register
 * a normalizer or handler onto a given type.
 */
public final class Kernel {
  private final ExceptionHandler dispatchingExceptionHandler;
  private final Map<Class<?>, TypeDispatcher<?>> typeDispatchers = new HashMap<>();

  private List<FireEvent<?>> pending = null;
  
  public Kernel() {
    this.dispatchingExceptionHandler = new FiringExceptionHandler(this);
        
    // Set-up the typeDispatcher Exception which requires a special configuration
    TypeDispatcher<Throwable> exceptionDispatcher = 
        new TypeDispatcher<>(WrappingExceptionHandler.INSTANCE);
    
    exceptionDispatcher.addHandler(RethrowHandler.INSTANCE);
    
    this.registerTypeDispatcher(Throwable.class, exceptionDispatcher);
  }
  
  @Deprecated
  public final FlexNexusEmitter asFlexEmitter() {
    return new FlexNexusEmitter() {
      @Override
      public <T> void fire(final Class<T> type, final T value) throws LogProcessingException {
        Kernel.this.fire(type, value);
      }
    };
  }
  
  public final <T> NexusEmitter<T> asTypeEmitter(final Class<T> eventType) {
    return new NexusEmitter<T>() {
      @Override
      public final void fire(final T value) {
        Kernel.this.fire(eventType, value);
      }
    };
  }
  
  public final <T> NexusHandledMarker<T> asTypeMarker(final Class<T> eventType) {
    return new NexusHandledMarker<T>() {
      @Override
      public final boolean isActivated() {
        return Kernel.this.hasUnhandler(eventType);
      }
      
      @Override
      public final void mark(final T value) {
        Kernel.this.markHandled(eventType, value);
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  private final <T> void fire(final Class<T> type, final T data) throws LogProcessingException {
    if ( this.pending != null ) {
      this.pending.add(new FireEvent<T>(type, data));
      return;
    }
    
    this.pending = new ArrayList<FireEvent<?>>();
    try {
      this.getOrCreateTypeDispatcher(type).fire(this, data);
    } finally {
      List<FireEvent<?>> priorPending = this.pending;
      this.pending = null;
      
      for ( FireEvent<?> fireEvent: priorPending ) {
        @SuppressWarnings("rawtypes")
        FireEvent erasedEvent = fireEvent;
        
        this.fire(erasedEvent.type, erasedEvent.data);
      }
    }
  }
  
  private final <T> boolean hasUnhandler(final Class<T> type) {
    TypeDispatcher<T> dispatcher = this.getTypeDispatcher(type);
    return dispatcher != null && dispatcher.hasUnhandler();
  }
  
  private final <T> void markHandled(final Class<T> type, final T data) {
    TypeDispatcher<T> dispatcher = this.getTypeDispatcher(type);
    if ( dispatcher != null ) dispatcher.markHandled(data);
  }
  
  public final <T> void normalize(
    final Class<T> type,
    final NexusNormalizer<T> normalizer)
  {
    this.getOrCreateTypeDispatcher(type).addNormalizer(normalizer);
  }
  
  public final <T> void handle(final Class<T> type, final NexusHandler<? super T> handler) {
    this.getOrCreateTypeDispatcher(type).addHandler(handler);
  }
  
  public final <T> void remove(final Class<T> type, final NexusHandler<? super T> handler) {
    this.getTypeDispatcher(type).removeHandler(handler);
  }
  
  @SuppressWarnings("unchecked")
  public final <T> void handle(final FlexNexusHandler handler) {
    this.handle(handler.type(), handler);
  }
  
  @SuppressWarnings("unchecked")
  public final <T> void remove(final FlexNexusHandler handler) {
    this.remove(handler.type(), handler);
  }
  
  public final <T> void unhandle(final Class<T> type, final NexusUnhandler<? super T> unhandler) {
    this.getOrCreateTypeDispatcher(type).addUnhandler(unhandler);
  }
  
  public final <T> void remove(final Class<T> type, final NexusUnhandler<? super T> unhandler) {
    this.getTypeDispatcher(type).removeUnhandler(unhandler);
  }
  
  
  public final void init(final Nexus ctx) {
    for ( TypeDispatcher<?> dispatcher: this.typeDispatchers.values() ) {
      dispatcher.init(ctx);
    }
  }
  
  public final void finish() {
    for ( TypeDispatcher<?> dispatcher: this.typeDispatchers.values() ) {
      dispatcher.finish();
    }
  }
  
  private final <T> TypeDispatcher<T> getTypeDispatcher(final Class<T> type) {
    TypeDispatcher<T> existingDispatcher = (TypeDispatcher<T>)this.typeDispatchers.get(type);
    return existingDispatcher;
  }
  
  private final <T> TypeDispatcher<T> getOrCreateTypeDispatcher(final Class<T> type) {
    TypeDispatcher<T> existingDispatcher = this.getTypeDispatcher(type);
    if ( existingDispatcher != null ) {
      return existingDispatcher;
    }
    
    TypeDispatcher<T> newDispatcher = new TypeDispatcher<T>(this.dispatchingExceptionHandler);

    // both of these scan the existing map of registered dispatchers, 
    // so this needs to be done before the put below
    this.linkExistingParents(type, newDispatcher);
    this.linkExistingChildren(type, newDispatcher);
    
    this.typeDispatchers.put(type, newDispatcher);
    return newDispatcher;
  }
  
  private final <T> void registerTypeDispatcher(final Class<T> type, final TypeDispatcher<T> newDispatcher) {
    this.typeDispatchers.put(type, newDispatcher);
  }

  /**
   * Registers a Handler on the newDispatcher that connects it to all existing parent handlers
   */
  private final <T> void linkExistingParents(
    final Class<T> type,
    final TypeDispatcher<T> newDispatcher)
  {
    final List<TypeDispatcher<? super T>> superDispatchers = new ArrayList<>();
    
    for ( Map.Entry<Class<?>, TypeDispatcher<?>> entry: this.typeDispatchers.entrySet() ) {
      Class<?> curType = entry.getKey();
      if ( !curType.isAssignableFrom(type) ) continue;
      
      @SuppressWarnings("unchecked")
      TypeDispatcher<? super T> curSuperDispatcher = (TypeDispatcher<? super T>)entry.getValue();
      superDispatchers.add(curSuperDispatcher);
    }
    
    if ( !superDispatchers.isEmpty() ) {
      newDispatcher.addHandler(new SuperHandler<T>(this, superDispatchers));
    }
  }
  
  /**
   * Registers a Handler on the newDispatcher that connects it to all existing parent handlers
   */
  private final <T> void linkExistingChildren(
    final Class<T> type,
    final TypeDispatcher<T> dispatcher)
  {
    ExtendsHandler<T> handler = null;
    
    for ( Map.Entry<Class<?>, TypeDispatcher<?>> entry: this.typeDispatchers.entrySet() ) {
      Class<?> curType = entry.getKey();
      if ( !type.isAssignableFrom(curType) ) continue;
      
      @SuppressWarnings("unchecked")
      TypeDispatcher<? extends T> childDispatcher = (TypeDispatcher<? extends T>)entry.getValue();
      
      if ( handler == null ) {
        handler = new ExtendsHandler<T>(this, dispatcher);
      }
      childDispatcher.addHandler(handler);
    }
  }
  

  static final class FireEvent<T> {
    final Class<T> type;
    final T data;
    
    public FireEvent(final Class<T> type, final T event) {
      this.type = type;
      this.data = event;
    }
    
    @Override
    public final String toString() {
      return this.type + " " + this.data;
    }
  }
  
  
  private static final class SuperHandler<T> implements NexusHandler<T> {
    private final Kernel engine;
    private final List<TypeDispatcher<? super T>> superDispatchers;
    
    SuperHandler(
      final Kernel engine,
      final List<TypeDispatcher<? super T>> superDispatchers)
    {
      this.engine = engine;
      this.superDispatchers = superDispatchers;
    }

    public void handle(final T value) throws Exception {
      for ( TypeDispatcher<? super T> superDispatcher: this.superDispatchers) {
        superDispatcher.fire(this.engine, value);
      }
    }
  }
  
  // A bit of dirtiness to keep the external API simple.
  // Normally init methods are called by EventEngine but inside CoreEventEngine,
  // there's no EventEngine to perform the necessary init call.
  // So inside CoreEventEngine, the CoreEventEngine is passed at construction 
  // if necessary and init is overridden as unsupported operation.
  private static abstract class CoreHandler<T> implements NexusHandler<T> {
    @Override
    public void init(final Nexus engine) throws Exception {
      throw new UnsupportedOperationException();
    }
  }
  
  private static final class RethrowHandler extends CoreHandler<Throwable> {
    private static final RethrowHandler INSTANCE = new RethrowHandler();
        
    @Override
    public final void handle(Throwable value) throws Exception {
      if ( value instanceof Error ) {
        throw (Error)value;
      } else if ( value instanceof Exception ) {
        throw (Exception)value;
      } else {
        throw new IllegalStateException();
      }
    }
  }
  
  private static final class WrappingExceptionHandler extends ExceptionHandler {
    private static final WrappingExceptionHandler INSTANCE = new WrappingExceptionHandler();
    
    @Override
    public final void handle(final Throwable cause) throws LogProcessingException {
      if ( cause instanceof Error ) {
        throw (Error)cause;
      } else if ( cause instanceof RuntimeException ) {
        throw (RuntimeException)cause;
      } else if ( cause instanceof LogProcessingException ) {
        throw (LogProcessingException)cause;
      } else {
        throw new LogProcessingException(cause);
      }
    }
  }
  
  private static final class FiringExceptionHandler extends ExceptionHandler {
    private final NexusEmitter<Throwable> emitter;
    
    public FiringExceptionHandler(final Kernel engine) {
      this.emitter = engine.asTypeEmitter(Throwable.class);
    }
    
    @Override
    public final void handle(final Throwable cause) throws LogProcessingException {
      this.emitter.fire(cause);
    }
  }
  
  private static final class ExtendsHandler<T> extends CoreHandler<T> {
    private final Kernel engine;
    private final TypeDispatcher<T> dispatcher;
    
    ExtendsHandler(
      final Kernel engine,
      final TypeDispatcher<T> dispatcher)
    {
      this.engine = engine;
      this.dispatcher = dispatcher;
    }
    
    public void handle(
      final T value)
      throws Exception
    {
      this.dispatcher.fire(this.engine, value);
    }
  }
}
