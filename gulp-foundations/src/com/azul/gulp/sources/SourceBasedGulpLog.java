package com.azul.gulp.sources;

import java.util.Collections;
import java.util.List;

import com.azul.gulp.Emitter;
import com.azul.gulp.ProcessingException;
import com.azul.gulp.Processor;
import com.azul.gulp.foundations.GulpLogBase;
import com.azul.gulp.foundations.GulpLogExtension;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusEmitter;
import com.azul.gulp.nexus.NexusHandledMarker;
import com.azul.gulp.nexus.NexusHandler;
import com.azul.gulp.nexus.Plugin;

public abstract class SourceBasedGulpLog<E extends GulpLogExtension<E>, S> extends GulpLogBase<E> {
  private final SourceConverterPlugin<S> sourcePlugin;
  
  public SourceBasedGulpLog(final Source<S> source) {
    this.sourcePlugin = new SourceConverterPlugin<S>(source);
  }
  
  protected SourceBasedGulpLog(final Source<S> source, final PipelineConfiguration normalizers) {
    super(normalizers);
    this.sourcePlugin = new SourceConverterPlugin<S>(source);
  }
  
  @Override
  public final E prefetch() {
    try {
      this.source().prefetch();
    } catch ( ProcessingException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ProcessingException(e);
    }
    
    @SuppressWarnings("unchecked")
    E casted = (E)this;
    return casted;
  }
  
  protected abstract E createOffspring(final Source<S> source);
  
  @Override
  protected abstract E createOffspring(final PipelineConfiguration normalizers);
  
  protected final <T extends Source<S>> T source() {
    @SuppressWarnings("unchecked")
    T casted = (T)this.sourcePlugin.source;
    return casted;
  }
  
  protected final Class<S> coreType() {
    return this.source().coreType();
  }
  
  @Override
  protected final List<Class<?>> coreTypes() {
    return Collections.<Class<?>>singletonList(this.coreType());
  }
  
  @Override
  protected List<Plugin> additionalPlugins() {
    return Collections.<Plugin>singletonList(this.sourcePlugin);
  }
  
  @Override
  protected void run(final Nexus engine) throws Exception {
    final NexusEmitter<S> emitter = engine.getEmitter(this.coreType());
    
    this.source().forEach(new Processor<S>() {
      @Override
      public final void process(final S input) {
        emitter.fire(input);
      }
    });
  }
  
  private static final class SourceConverterPlugin<T> extends Plugin {
    final Source<T> source;
    
    SourceConverterPlugin(final Source<T> source) {
      this.source = source;
    }
    
    @Override
    public <V> boolean handleEventRequest(
      final Nexus nexus,
      final Class<V> requiredType)
      throws Exception
    {
      Class<T> coreType = this.source.coreType();
      
      final Converter<T, V> converter = this.source.converterFor(nexus, requiredType);
      if ( converter == null ) return false;
      
      nexus.handle(coreType, new NexusHandler<T>() {
        private NexusHandledMarker<T> marker;
        private Emitter<V> emitter;
        
        @Override
        public void init(final Nexus nexus) throws Exception {
          converter.init(nexus);

          this.marker = nexus.getMarker(coreType);
          
          NexusEmitter<V> nexusEmitter = nexus.getEmitter(requiredType);
          this.emitter = nexusEmitter::fire;
        }
        
        @Override
        public final void handle(final T input) throws Exception {
          // TODO: Try to determine a way of reducing the overhead of unhandled tracking
          if ( this.marker.isActivated() ) {
            Emitter<V> markingEmitter = (output) -> {
              this.marker.mark(input);
              
              this.emitter.fire(output);
            };
            converter.convert(input, markingEmitter);
          } else {          
            converter.convert(input, this.emitter);
          }
        }
      });
      return true;
    }
  }
}
