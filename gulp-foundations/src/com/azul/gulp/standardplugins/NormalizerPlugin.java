package com.azul.gulp.standardplugins;

import java.lang.reflect.Constructor;

import com.azul.gulp.Gulp;
import com.azul.gulp.Normalizer;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusNormalizer;
import com.azul.gulp.nexus.Plugin;

public final class NormalizerPlugin extends Plugin {
  @Override
  public <V> void onEventRequest(
    final Nexus engine,
    final Class<V> requiredType) throws Exception
  {
    Gulp.Normalize normalizeAnno = requiredType.getAnnotation(Gulp.Normalize.class);
    if ( normalizeAnno == null ) return;
    
    Normalizer<V> normalizer = createNormalizer(requiredType, normalizeAnno);
    
    // ugly bit of duplication with Normalizers
    engine.normalize(requiredType, new NexusNormalizer<V>() {
      @Override
      public void init(final Nexus nexus) throws Exception {
        nexus.inject(normalizer);
      }
      
      @Override
      public final V normalize(final V value) throws Exception {
        return normalizer.normalize(value);
      }
    });
  }
  
  @Override
  public final <V> boolean connect(final Nexus engine, final Object object) throws Exception {
    return false;
  }
  
  private static <T> Normalizer<T> createNormalizer(
    final Class<T> type,
    Gulp.Normalize normalizeAnno)
  {
    Constructor<? extends Normalizer<?>> ctor;
    try {
      ctor = normalizeAnno.value().getDeclaredConstructor();
    } catch ( NoSuchMethodException e ) {
      throw new IllegalStateException(e);
    }
    ctor.setAccessible(true);
    
    try {
      @SuppressWarnings("unchecked")
      Normalizer<T> casted = (Normalizer<T>)ctor.newInstance();
      return casted;
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException(e);
    }
  }
}
