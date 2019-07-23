package com.azul.gulp.standardplugins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.azul.gulp.Gulp;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.nexus.NexusHandler;
import com.azul.gulp.nexus.NexusNormalizer;
import com.azul.gulp.nexus.Plugin;

public class EventCapturePlugin extends Plugin {
  @Override
  public <V> void onEventRequest(
    final Nexus nexus,
    final Class<V> requiredType) throws Exception
  {
    for ( Field field: requiredType.getFields() ) {
      Gulp.Capture captureAnno = field.getAnnotation(Gulp.Capture.class);
      if ( captureAnno == null ) continue;
      
      Class<?> fieldType = field.getType();
      field.setAccessible(true);
      
      EventFieldConnector connector = new EventFieldConnector(field);
      nexus.handleOptional(fieldType, connector);
      nexus.normalize(requiredType, connector);
    }
    
    for ( Field field: requiredType.getDeclaredFields() ) {
      if ( Modifier.isPublic(field.getModifiers()) ) continue;  // covered above
      
      Gulp.Capture captureAnno = field.getAnnotation(Gulp.Capture.class);
      if ( captureAnno == null ) continue;
      
      Class<?> fieldType = field.getType();
      field.setAccessible(true);
      
      EventFieldConnector connector = new EventFieldConnector(field);
      nexus.handleOptional(fieldType, connector);
      nexus.normalize(requiredType, connector);
    }
  }
  
  public <V> void onConnect(
    final Nexus nexus,
    final Object object) throws Exception
  {
    for ( Field field: object.getClass().getFields() ) {
      Gulp.Capture captureAnno = field.getAnnotation(Gulp.Capture.class);
      if ( captureAnno == null ) continue;
      
      Class<?> fieldType = field.getType();
      field.setAccessible(true);
      
      nexus.handleOptional(fieldType, new InjectedObjectConnector(object, field));
    }

    for ( Field field: object.getClass().getDeclaredFields() ) {
      if ( Modifier.isPublic(field.getModifiers()) ) continue; // covered above
      
      Gulp.Capture captureAnno = field.getAnnotation(Gulp.Capture.class);
      if ( captureAnno == null ) continue;
      
      Class<?> fieldType = field.getType();
      field.setAccessible(true);
      
      nexus.handleOptional(fieldType, new InjectedObjectConnector(object, field));
    }
  }
  
  private final class EventFieldConnector<R, T> 
    implements NexusHandler<R>, NexusNormalizer<T>
  {
    private final Field field;
    private R last = null;
    
    public EventFieldConnector(final Field field) {
      this.field = field;
    } 
    
    @Override
    public final void handle(final R value) throws Exception {
      if ( value == null ) throw new IllegalArgumentException();
      
      this.last = value;
    }
    
    @Override
    public final T normalize(T target) throws Exception {
      T cur = (T)this.field.get(target);
      if ( cur != null && this.last == null ) return target;
      
      this.field.set(target, this.last);
      return target;
    }
  }
  
  private final class InjectedObjectConnector<T> implements NexusHandler<T> {
    private final Object target;
    private final Field field;

    public InjectedObjectConnector(final Object target, final Field field) {
      this.target = target;
      this.field = field;
    }
    
    @Override
    public final void handle(final T value) throws Exception {
      this.field.set(this.target, value);
    }
  }
}
