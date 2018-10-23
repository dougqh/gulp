package com.azul.gulp.nexus;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azul.gulp.ConfigurationException;
import com.azul.gulp.Emitter;
import com.azul.gulp.HandledMarker;
import com.azul.gulp.inject.ConstructorInjector;
import com.azul.gulp.inject.FieldInjector;
import com.azul.gulp.inject.InjectionAware;
import com.azul.gulp.kernel.Kernel;
import com.azul.gulp.reflect.AnnotationFinder;
import com.azul.gulp.reflect.BasicAnnotationFinder;

public final class NexusImpl implements Nexus {
  private final Kernel kernel = new Kernel();
  private final Plugin plugins;
  
  private final Set<Class<?>> availableTypes = new HashSet<>();
  private final Set<Class<?>> requestedTypes = new HashSet<>();
  private final Map<Class<?>, Object> ctxObjects = new HashMap<>();
  
  public NexusImpl(
    final Collection<Class<?>> coreTypes,
    final List<Plugin> plugins)
  {
    this.availableTypes.addAll(coreTypes);
    this.plugins = new CompositePlugin(plugins);
  }
  
  @Override
  public <A extends Annotation> List<A> findAnnotationsFor(
	Class<?> klass,
	Class<A> annoKlass)
  {
	return this.get(AnnotationFinder.class).findAnnotationsFor(klass, annoKlass);
  }
  
  public final void inject(final Object obj) {
    FieldInjector injector = new FieldInjector(obj);
    for ( Type requiredType: injector.requires() ) {
      injector.inject(requiredType, this.getOrCreate(requiredType));
    }
    
    if ( obj instanceof InjectionAware ) {
      InjectionAware injectionAware = (InjectionAware)obj;
      injectionAware.onInject(this);
    }
    
    try {
      this.plugins.onConnect(this, obj);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
  }
  
  @Override
  public <T> void normalize(
    final Class<T> type,
    final NexusNormalizer<T> normalizer)
  {
    this.require(type);

    this.inject(normalizer);
    try {
      normalizer.init(this);
    } catch ( ConfigurationException e ) {
      throw (ConfigurationException)e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
    
    this.kernel.normalize(type, normalizer);
  }
  
  @Override
  public <T> NexusEmitter<T> getEmitter(final Class<T> type) {
    this.supply(type);
    
    return this.kernel.asTypeEmitter(type);
  }
  
  @Override
  public <T> NexusHandledMarker<T> getMarker(final Class<T> type) {
    return this.kernel.asTypeMarker(type);
  }
  
  @Override
  public void connect(final Object obj) {
    boolean connected;
    
    try {
      connected = this.plugins.connect(this, obj);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
    
    if ( !connected ) throw new ConfigurationException("connect!");
    
    try {
      this.plugins.onConnect(this, obj);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
  }
  
  @Override
  public <V> void handle(final Class<V> type, final NexusHandler<? super V> handler) {
    this.require(type);
    
    this.inject(handler);
    try {
      handler.init(this);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
    
    this.kernel.handle(type, handler);
  }
  
  @Override
  public <V> void handleOptional(
    final Class<V> type,
    final NexusHandler<? super V> handler)
  {
    boolean satisfied = this.request(type);
    if ( !satisfied ) return;
    
    this.inject(handler);
    try {
      handler.init(this);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(e);
    }
    
    this.kernel.handle(type, handler);
  }
  
  @Override
  public <V> void remove(Class<V> type, NexusHandler<? super V> handler) {
    this.kernel.remove(type, handler);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public final void handle(final FlexNexusHandler handler) {
    this.handle(handler.type(), handler);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public final void handleOptional(final FlexNexusHandler handler) {
    this.handleOptional(handler.type(), handler);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void remove(final FlexNexusHandler handler) {
    this.remove(handler.type(), handler);
  }
  
  @Override
  public final <T> void unhandle(
    final Class<T> type,
    final NexusUnhandler<? super T> unhandler) throws ConfigurationException
  {
    this.kernel.unhandle(type, unhandler);
  }
  
  @Override
  public final <T> void remove(
    final Class<T> type,
    final NexusUnhandler<? super T> unhandler) throws ConfigurationException
  {
    this.kernel.remove(type, unhandler);
  }
  
  @Override
  public <T> T get(Class<T> type) {
    if ( type.equals(FlexNexusEmitter.class) ) {
      return cast(this.kernel.asFlexEmitter());
    } else {
      return this.getOrCreate(type);
    }
  }
  
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public final <T> T get(Class<T> rawType, final Class<?>... typeParams) {
    if ( rawType.equals(Emitter.class) && typeParams.length == 1 ) {
      NexusEmitter nexusEmitter = this.getEmitter(typeParams[0]);
      T casted = (T)new Emitter() {
        @Override
        public void fire(Object value) {
          nexusEmitter.fire(value);
        }
      };
      return casted;
    } else if ( rawType.equals(HandledMarker.class) && typeParams.length == 1 ) {
      NexusHandledMarker nexusMarker = this.getMarker(typeParams[0]);
      T casted = (T)new HandledMarker() {
        @Override
        public void mark(Object value) {
          nexusMarker.mark(value);
        }
      };
      return casted;
    } else {
      throw new ConfigurationException(rawType, typeParams);
    }
  }
  
  private static final <T> T cast(final Object value) {
    @SuppressWarnings("unchecked")
    T casted = (T)value;
    return casted;
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  final Object getOrCreate(final Type type) {
    if ( type instanceof Class ) {
      return this.getOrCreate((Class)type);
    } else if ( type instanceof ParameterizedType ) {
      ParameterizedType paramType = (ParameterizedType)type;
      return this.get((Class)paramType.getRawType(), toClasses(paramType.getActualTypeArguments()));
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final Class<?>[] toClasses(final Type[] types) {
    Class[] rawTypes = new Class[types.length];
    for ( int i = 0; i < types.length; ++i ) {
      rawTypes[i] = (Class)types[i];
    }
    return rawTypes;
  }
  
  @Override
  public <T> T create(Class<T> type) {
    ConstructorInjector<T> injector = new ConstructorInjector<>(type);
    for ( Type requiredType: injector.requires() ) {
      injector.inject(requiredType, this.getOrCreate(requiredType));
    }
    
    T newObj = injector.make();
    try {
      this.plugins.connect(this, newObj);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(type, e);
    }
    
    return newObj;
  }
  
  @SuppressWarnings("unchecked")  
  final <T> T getOrCreate(final Class<T> type) {
    T existingObj = (T)this.ctxObjects.get(type);
    if ( existingObj != null ) return existingObj;

    if ( type.equals(AnnotationFinder.class) ) {
      return (T)BasicAnnotationFinder.INSTANCE;
    }
    
    T newObj = this.create(type);
    this.ctxObjects.put(type, newObj);
    return newObj;
  }
  
  public final void require(final Class<?>... eventTypes) 
    throws ConfigurationException
  {
    for ( Class<?> eventType: eventTypes ) {
      this.require(eventType);
    }
  }
  
  private final void supply(final Class<?> type)
    throws ConfigurationException
  {
    if ( !this.availableTypes.add(type) ) return;
    
    try { 
      this.plugins.onEventRequest(this, type);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(type, e);
    }
  }
  
  public final boolean request(final Class<?>... eventTypes)
    throws ConfigurationException
  {
    boolean satisfied = true;
    for ( Class<?> eventType: eventTypes ) {
      satisfied |= this.request(eventType);
    }
    return satisfied;
  }
  
  @Override
  public void produces(Class<?>... types) {
    this.availableTypes.addAll(Arrays.asList(types));
  }
  
  final void require(final Collection<? extends Class<?>> eventTypes)
    throws ConfigurationException
  {
    for ( Class<?> eventType: eventTypes ) {
      this.require(eventType);
    }
  }
  
  final <V> boolean request(final Class<V> type)
    throws ConfigurationException
  {
    if ( this.availableTypes.contains(type) ) return true;
    
    // catch for already request type to prevent repeats -- 
    //   benign but wasteful
    if ( !this.requestedTypes.add(type) ) return true;
    
    boolean handled;
    try {
      handled = this.plugins.handleEventRequest(this, type);
    } catch ( ConfigurationException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ConfigurationException(type, e);
    }
    
    if ( handled ) {
      if ( !this.availableTypes.contains(type) ) {
        throw new IllegalStateException("required type unvailable: " + type);
      }
    }
    
    return handled;
  }
  
  final <V> void require(final Class<V> type)
    throws ConfigurationException
  {
    boolean satisfied = this.request(type);
    if ( !satisfied ) {
      throw new ConfigurationException(type);
    }
  }
}