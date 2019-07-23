package com.azul.gulp.foundations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azul.gulp.Enricher;
import com.azul.gulp.GenericAnalyzer;
import com.azul.gulp.GenericProcessor;
import com.azul.gulp.GulpLogStream;
import com.azul.gulp.GulpPairStream;
import com.azul.gulp.LogProcessingException;
import com.azul.gulp.LogProcessor;
import com.azul.gulp.Normalizer;
import com.azul.gulp.PackagedAnalyzer;
import com.azul.gulp.Pair;
import com.azul.gulp.PairPredicate;
import com.azul.gulp.Predicate;
import com.azul.gulp.Processor;
import com.azul.gulp.Result;
import com.azul.gulp.StreamProcessingException;
import com.azul.gulp.ThrowingFunction;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusHandler;
import com.azul.gulp.nexus.NexusImpl;
import com.azul.gulp.nexus.NexusUnhandler;
import com.azul.gulp.nexus.Plugin;
import com.azul.gulp.properties.FieldPropertyDetector;
import com.azul.gulp.properties.KeyProperty;
import com.azul.gulp.properties.Property;
import com.azul.gulp.properties.PropertyDetector;
import com.azul.gulp.properties.PropertyUtils;
import com.azul.gulp.sources.PipelineConfiguration;
import com.azul.gulp.standardplugins.ComplexEventPlugin;
import com.azul.gulp.standardplugins.EventCapturePlugin;
import com.azul.gulp.standardplugins.InnerSubclassPlugin;
import com.azul.gulp.standardplugins.NormalizerPlugin;
import com.azul.gulp.standardplugins.ProcessorPlugin;
import com.azul.gulp.standardplugins.UnhandledPlugin;


public abstract class GulpLogBase<C extends GulpLogExtension<C>>
  implements GulpLogExtension<C>
{
  private static final int DEFAULT_COLLECTION_SIZE = 64;
  
  private static final PipelineConfiguration PLAIN_CONFIG = new PipelineConfiguration();
  
  private final PipelineConfiguration pipelineConfig;
  
  protected GulpLogBase() {
    this(PLAIN_CONFIG);
  }
  
  protected GulpLogBase(final PipelineConfiguration normalizers) {
    this.pipelineConfig = normalizers;
  }
  
  protected static final Plugin[] NO_PLUGINS = {};
  
  protected abstract List<Class<?>> coreTypes();
    
  final List<Plugin> corePlugins() {
    return Arrays.asList(
      new InnerSubclassPlugin(),
      new ComplexEventPlugin(),
      
      new ProcessorPlugin(),
      new UnhandledPlugin(),
      new NormalizerPlugin(),
      new EventCapturePlugin()
    );
  }
  
  protected List<Plugin> additionalPlugins() {
    return Collections.emptyList();
  }
  
  private final Nexus createEngine() {
    List<Plugin> corePlugins = this.corePlugins();
    List<Plugin> additionalPlugins = this.additionalPlugins();
    
    List<Plugin> plugins = new ArrayList<Plugin>(
        corePlugins.size() + additionalPlugins.size());
    plugins.addAll(corePlugins);
    plugins.addAll(additionalPlugins);
    
    NexusImpl engine = new NexusImpl(
      this.coreTypes(),
      plugins);
    
    this.pipelineConfig.configure(engine);
    
    return engine;
  }
  
  protected final PipelineConfiguration normalizers() {
    return this.pipelineConfig;
  }
  
  protected abstract C createOffspring(final PipelineConfiguration pipelineConfig);
  
  protected abstract void run(final Nexus engine) throws Exception;

  
  @Override
  public final <T> C normalize(final Class<T> type, final Normalizer<T> normalizer) {
    return this.createOffspring(this.pipelineConfig.add(type, normalizer));
  }
  
  @Override
  public final <T, V> C enrich(
    final Class<T> inputType,
    final Class<V> enrichmentType,
    final Enricher<? super T, ? extends V> enricher)
  {
    return this.createOffspring(this.pipelineConfig.append(inputType, enrichmentType, enricher));
  }
  
  @Override
  public final <T> GulpLogStream<T> select(final Class<? extends T> dataClass) {
    return new GulpLogStreamImpl<T>(new StreamCore<T>() {
      @Override
      protected void processImpl(final Processor<? super T> processor) throws StreamProcessingException {
        Nexus engine = GulpLogBase.this.createEngine();
        engine.handle(dataClass, new NexusHandler<T>() {
          @Override
          public void init(final Nexus engine) throws Exception {
            engine.inject(processor);
          }
          
          @Override
          public void handle(final T value) throws Exception {
            processor.process(value);
          }
        });
        try {
          GulpLogBase.this.run(engine);
        } catch ( StreamProcessingException e )  {
          throw e;
        } catch ( Exception e ) {
          throw new StreamProcessingException(e);
        }
      }
    });
  }
  

  
  @Override
  public <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass,
    final PairPredicate<? super F, ? super S> predicateFn)
  {
    return new GulpPairStreamImpl<F, S>(new StreamCore<Pair<F, S>>() {
      @Override
      protected void processImpl(final Processor<? super Pair<F, S>> processor)
        throws StreamProcessingException
      {
        Nexus engine = GulpLogBase.this.createEngine();
        
        Set<F> firsts = new HashSet<>(DEFAULT_COLLECTION_SIZE);
        Set<S> seconds = new HashSet<>(DEFAULT_COLLECTION_SIZE);
        
        engine.handle(firstDataClass, new NexusHandler<F>() {
          @Override
          public void init(final Nexus engine) throws Exception {
            // arbitrarily chose to inject processor & predicate in 
            // first handler could be done in second handler, too.
            engine.inject(processor);
            
            engine.inject(predicateFn);
          }
          
          @Override
          public void handle(final F newFirst) throws Exception {
            for ( S second: seconds ) {
              if ( predicateFn.matches(newFirst, second) ) {
                processor.process(Pair.make(newFirst, second));
              }
            }
            
            firsts.add(newFirst);
          }
        });
        
        engine.handle(secondDataClass, new NexusHandler<S>() {
          @Override
          public void handle(final S newSecond) throws Exception {
            for ( F first: firsts ) {
              if ( predicateFn.matches(first, newSecond) ) {
                // dispatch pair
                processor.process(Pair.make(first, newSecond));
              }
            }
            
            seconds.add(newSecond);
          }
        });
        
        try {
          GulpLogBase.this.run(engine);
        } catch ( StreamProcessingException e )  {
          throw e;
        } catch ( Exception e ) {
          throw new StreamProcessingException(e);
        }
      }
    });
  }
  
  @Override
  public final <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass,
    final Predicate<? super Pair<? super F, ? super S>> predicateFn)
  {
    return this.join(
      firstDataClass,
      secondDataClass, 
      (f, s) -> predicateFn.matches(Pair.make(f, s)));
  }
  
  @Override
  public final <F, S, K> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final ThrowingFunction<? super F, ? extends K> firstKeyFn,
    final Class<? extends S> secondDataClass,
    final ThrowingFunction<? super S, ? extends K> secondKeyFn)
  {
    return new GulpPairStreamImpl<F, S>(new StreamCore<Pair<F, S>>() {
      @Override
      protected void processImpl(final Processor<? super Pair<F, S>> processor)
        throws StreamProcessingException
      {
        Nexus engine = GulpLogBase.this.createEngine();
        
        Map<K, F> firstsMap = new HashMap<>(DEFAULT_COLLECTION_SIZE);
        Map<K, S> secondsMap = new HashMap<>(DEFAULT_COLLECTION_SIZE);
        
        engine.handle(firstDataClass, new NexusHandler<F>() {
          @Override
          public void init(final Nexus engine) throws Exception {
            // arbitrarily chose to inject processor in 
            // first handler could be done in second handler, too.
            engine.inject(processor);
            
            engine.inject(firstKeyFn);
          }
          
          @Override
          public void handle(final F newFirst) throws Exception {
            K key = firstKeyFn.apply(newFirst);
            firstsMap.put(key, newFirst);
            
            S second = secondsMap.get(key);
            if ( second != null ) {
              processor.process(Pair.make(newFirst, second));
            }
          }
        });
        
        engine.handle(secondDataClass, new NexusHandler<S>() {
          @Override
          public void init(final Nexus engine) throws Exception {
            engine.inject(secondKeyFn);
          }
          
          @Override
          public void handle(final S newSecond) throws Exception {
            K key = secondKeyFn.apply(newSecond);
            secondsMap.put(key, newSecond);
            
            F first = firstsMap.get(key);
            if ( first != null ) {
              processor.process(Pair.make(first, newSecond));
            }
          }
        });
        
        try {
          GulpLogBase.this.run(engine);
        } catch ( StreamProcessingException e )  {
          throw e;
        } catch ( Exception e ) {
          throw new StreamProcessingException(e);
        }
      }
    });
  }
  
  @Override
  public <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass)
  {
    // TODO: Should really register property detectors through plug-in mechanism
    PropertyDetector propDetector = new FieldPropertyDetector();
    
    Map<String, Property> firstProps = propDetector.getProperties(firstDataClass);
    Map<String, Property> secondProps = propDetector.getProperties(secondDataClass);
    
    Map<Class<?>, KeyProperty> firstKeys = PropertyUtils.findKeys(firstProps);
    Map<Class<?>, KeyProperty> secondKeys = PropertyUtils.findKeys(secondProps);
    
    // First see if there's a single common key type
    Set<Class<?>> commonKeyTypes = new HashSet<>(firstKeys.keySet());
    commonKeyTypes.retainAll(secondKeys.keySet());
    
    if ( commonKeyTypes.size() == 1 ) {
      Class<?> commonKeyType = commonKeyTypes.iterator().next();
      
      KeyProperty firstKey = firstKeys.get(commonKeyType);
      KeyProperty secondKey = secondKeys.get(commonKeyType);
      
      return this.join(firstDataClass, firstKey::get, secondDataClass, secondKey::get);
    }
    
    // Finally -- see if there's a single property with a common name
    HashSet<String> commonPropNames = new HashSet<>(firstProps.keySet());
    commonPropNames.retainAll(secondProps.keySet());
    
    if ( commonPropNames.size() == 1 ) {
      String commonPropName = commonPropNames.iterator().next();
      
      Property firstProp = firstProps.get(commonPropName);
      Property secondProp = secondProps.get(commonPropName);
      
      // TODO: type check property types?
      
      return this.join(
        firstDataClass, firstProp::get,
        secondDataClass, secondProp::get);
    }
    
    throw new IllegalStateException("Failed to auto-deduce join property");
  }
  
  public final <R> Result<R> analyze(PackagedAnalyzer<R> analyzer) {
    return new Result<R>(analyzer.analyze(this));
  }
  
  @Override
  public final <R> Result<R> analyze(GenericAnalyzer<R> analyzer) {
    this.process(analyzer);
    return new Result<R>(analyzer);
  }
  
  @Override
  public final <R> R analyzeAndGet(final GenericAnalyzer<R> analyzer) {
    return this.analyze(analyzer).get();
  }
  
  @Override
  @SuppressWarnings("rawtypes")
  public final Result process(final GenericProcessor processor) throws LogProcessingException {
    Nexus engine = this.createEngine();
    engine.connect(processor);
    
    try {
      this.run(engine);
    } catch ( LogProcessingException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new LogProcessingException(e);
    }
    
    return new Result(processor);
  }
  
  @Override
  public final Result process(LogProcessor.Provider provider) {
    return this.process(provider.get());
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public final Result process(LogProcessor logProcessor) throws LogProcessingException {
    final class NexusHandlerAdapter implements NexusHandler {
      private final Processor rawProcessor;
      
      public NexusHandlerAdapter(final Processor rawProcessor) {
        this.rawProcessor = rawProcessor;
      }
      
      @Override
      public void init(Nexus nexus) {
        nexus.inject(this.rawProcessor);
      }

      @Override
      public void handle(Object value) throws Exception {
        this.rawProcessor.process(value);
      }
    }
    
    final class NexusUnhandlerAdapter implements NexusUnhandler {
      private final Processor rawProcessor;
      
      public NexusUnhandlerAdapter(final Processor rawProcessor) {
        this.rawProcessor = rawProcessor;
      }
      
      @Override
      public void init(Nexus nexus) {
        nexus.inject(this.rawProcessor);
      }

      @Override
      public void unhandle(Object value) throws Exception {
        this.rawProcessor.process(value);
      }
    }
    
    Nexus engine = this.createEngine();
    // not handle through a plugin, so no connect
    engine.inject(logProcessor);
    
    for ( Class<?> klass: logProcessor.requiredTypes() ) {
      Class rawKlass = (Class)klass;
      
      Processor rawProcessor = (Processor)logProcessor.processorFor(klass);
      if ( rawProcessor != null ) {
        engine.handle(rawKlass, new NexusHandlerAdapter(rawProcessor));
      }
      
      Processor rawUnhandledProcessor = (Processor)logProcessor.unhandledProcessorFor(klass);
      if ( rawUnhandledProcessor != null ) {
        engine.unhandle(rawKlass, new NexusUnhandlerAdapter(rawUnhandledProcessor));
      }
    }
    
    for ( Class<?> klass: logProcessor.optionalTypes() ) {
      Class rawKlass = (Class)klass;
      
      Processor rawProcessor = (Processor)logProcessor.processorFor(klass);
      if ( rawProcessor != null ) {
        engine.handle(rawKlass, new NexusHandlerAdapter(rawProcessor));
      }
      
      Processor rawUnhandledProcessor = (Processor)logProcessor.unhandledProcessorFor(klass);
      if ( rawUnhandledProcessor != null ) {
        engine.unhandle(rawKlass, new NexusUnhandlerAdapter(rawUnhandledProcessor));
      }
    }
    
    try {
      this.run(engine);
    } catch ( LogProcessingException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new LogProcessingException(e);
    }
    
    return new Result(logProcessor);
  }
  
  @Override
  public final C preprocess(final GenericProcessor preprocessor) {
    return this.createOffspring(this.pipelineConfig.add(preprocessor));
  }
  
  @Override
  public <T> Result<T> get(final Class<T> analysis) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public C prefetch() {
    throw new UnsupportedOperationException("prefetch");
  }
}
