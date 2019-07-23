package com.azul.gulp;

import java.util.Collection;

import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.sources.Converter;
import com.azul.gulp.sources.PipelineConfiguration;
import com.azul.gulp.sources.Source;
import com.azul.gulp.sources.SourceBasedGulpLog;

final class CollectionBackedGulpLog<T> 
  extends SourceBasedGulpLog<CollectionBackedGulpLog<T>, T>
{
  public CollectionBackedGulpLog(
    Class<T> sourceType,
    Collection<? extends T> collection)
  {
    super(new Source<T>() {
      @Override
      public Class<T> coreType() {
        return sourceType;
      }

      @Override
      protected void forEachImpl(Processor<? super T> processor)
        throws Exception
      {
        for ( T cur: collection ) {
          processor.process(cur);
        }
      }

      @Override
      public <V> Converter<T, V> converterFor(final Nexus nexus, Class<V> type) {
        return null;
      }
      
      protected void prefetch() throws Exception {}
    });
  }
  
  protected CollectionBackedGulpLog(final Source<T> source, final PipelineConfiguration normalizers) {
    super(source, normalizers);
  }
  
  @Override
  protected final CollectionBackedGulpLog<T> createOffspring(final Source<T> source) {
    return new CollectionBackedGulpLog<T>(source, this.normalizers());
  }
  
  @Override
  protected final CollectionBackedGulpLog<T> createOffspring(final PipelineConfiguration normalizers) {
    return new CollectionBackedGulpLog<T>(this.source(), normalizers);
  }
}
