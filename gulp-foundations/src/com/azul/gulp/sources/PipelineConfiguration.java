package com.azul.gulp.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.azul.gulp.Enricher;
import com.azul.gulp.GenericProcessor;
import com.azul.gulp.Normalizer;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusEmitter;
import com.azul.gulp.nexus.NexusNormalizer;

public final class PipelineConfiguration {
  public static final PipelineConfiguration EMPTY = new PipelineConfiguration();
  
  private final List<Class<?>> normalizedTypes;
  private final List<NexusNormalizer<?>> normalizers;
  
  private final List<GenericProcessor> preprocessors;
  private final List<Class<? extends GenericProcessor>> preprocessorClasses;
  
  public PipelineConfiguration() {
    this.normalizedTypes = Collections.emptyList();
    this.normalizers = Collections.emptyList();
    
    this.preprocessors = Collections.emptyList();
    this.preprocessorClasses = Collections.emptyList();
  }
  
  private <T> PipelineConfiguration(
    final PipelineConfiguration baseConfig,
    final Class<T> type,
    final NexusNormalizer<T> normalizer)
  {
    this.normalizedTypes = new ArrayList<Class<?>>(baseConfig.normalizedTypes.size() + 1);
    this.normalizers = new ArrayList<NexusNormalizer<?>>(baseConfig.normalizers.size() + 1);
    
    this.normalizedTypes.addAll(baseConfig.normalizedTypes);
    this.normalizers.addAll(baseConfig.normalizers);
    
    this.normalizedTypes.add(type);
    this.normalizers.add(normalizer);
    
    this.preprocessors = baseConfig.preprocessors;
    this.preprocessorClasses = baseConfig.preprocessorClasses;
  }
  
  private <T> PipelineConfiguration(
    final PipelineConfiguration baseConfig,
    final GenericProcessor preprocessor)
  {
    this.normalizedTypes = baseConfig.normalizedTypes;
    this.normalizers = baseConfig.normalizers;

    this.preprocessorClasses = baseConfig.preprocessorClasses;
    
    this.preprocessors = new ArrayList<>(baseConfig.preprocessors.size() + 1);
    this.preprocessors.addAll(baseConfig.preprocessors);
    this.preprocessors.add(preprocessor);
  }
  
  private <T> PipelineConfiguration(
    final PipelineConfiguration baseConfig,
    final Class<? extends GenericProcessor> preprocessorClass)
  {
    this.normalizedTypes = baseConfig.normalizedTypes;
    this.normalizers = baseConfig.normalizers;
    this.preprocessors = baseConfig.preprocessors;
    
    this.preprocessorClasses = new ArrayList<>(baseConfig.preprocessorClasses.size() + 1);
    this.preprocessorClasses.addAll(baseConfig.preprocessorClasses);
    this.preprocessorClasses.add(preprocessorClass);
  }
  
  private final <T> PipelineConfiguration add(
    final Class<T> type,
    final NexusNormalizer<T> normalizer)
  {
    return new PipelineConfiguration(this, type, normalizer);
  }
  
  public final <T> PipelineConfiguration add(
    final Class<T> type,
    final Normalizer<T> normalizer)
  {
    return this.add(type, new NexusNormalizer<T>() {
      @Override
      public void init(final Nexus nexus) throws Exception {
        nexus.inject(normalizer);
      }
      
      @Override
      public final T normalize(final T value) throws Exception {
        return normalizer.normalize(value);
      }
    });
  }
  
  public final <T, V> PipelineConfiguration append(
    final Class<T> inputType,
    final Class<V> outputType,
    final Enricher<? super T, ? extends V> enricher)
  {
    return this.add(inputType, new NexusNormalizer<T>() {
      private NexusEmitter<V> emitter;
      
      @Override
      public final void init(final Nexus ctx) throws Exception {
        ctx.inject(enricher);
        
        ctx.require(inputType);
        this.emitter = ctx.getEmitter(outputType);
      }
      
      public final T normalize(T value) throws Exception {
        V enrichment = enricher.enrichment(value);
        if ( enrichment != null ) {
          this.emitter.fire(enrichment);
        }
        
        return value;
      }
    });
  }
  
  public final PipelineConfiguration add(final GenericProcessor preprocessor) {
    return new PipelineConfiguration(this, preprocessor);
  }
  
  public final PipelineConfiguration addPreprocessor(
    final Class<? extends GenericProcessor> preprocessorClass)
  {
    return new PipelineConfiguration(this, preprocessorClass);
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  public final void configure(final Nexus engine) {
    Iterator<Class<?>> typesIterator = this.normalizedTypes.iterator();
    Iterator<NexusNormalizer<?>> normalizerIterator = this.normalizers.iterator();
    
    while ( typesIterator.hasNext() ) {
      Class type = typesIterator.next();
      NexusNormalizer normalizer = normalizerIterator.next();
      
      engine.normalize(type, normalizer);
    }
    
    for ( GenericProcessor preprocessor: this.preprocessors ) {
      engine.connect(preprocessor);
    }
    
    for ( Class<?> preprocessorClass: this.preprocessorClasses ) {
      engine.get(preprocessorClass);
    }
  }
}
