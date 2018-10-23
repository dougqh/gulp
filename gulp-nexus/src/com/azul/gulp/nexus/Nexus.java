package com.azul.gulp.nexus;

import com.azul.gulp.ConfigurationException;
import com.azul.gulp.inject.InjectionContext;
import com.azul.gulp.reflect.AnnotationFinder;

public interface Nexus extends InjectionContext, AnnotationFinder {
  public abstract void require(final Class<?>... types)
    throws ConfigurationException;
  
  public abstract boolean request(final Class<?>... types)
    throws ConfigurationException;
  
  // normalize(type, ...) implies require(type)
  public abstract <T> void normalize(
    final Class<T> type,
    final NexusNormalizer<T> normalizer);
  
  // Calling handle(type, ...) implies require(type)
  public abstract <T> void handle(
    final Class<T> type,
    final NexusHandler<? super T> handler)
    throws ConfigurationException;
  
  // Calling optionalHandle(type, ...) implies request(type)
  public abstract <T> void handleOptional(
    final Class<T> type,
    final NexusHandler<? super T> handler)
    throws ConfigurationException;
  
  public abstract <T> void remove(
    final Class<T> type,
    final NexusHandler<? super T> handler);
  
  public abstract void handle(final FlexNexusHandler handler);
  
  public abstract void handleOptional(final FlexNexusHandler handler);
  
  public abstract void remove(final FlexNexusHandler handler);
  
  public abstract <T> void unhandle(
    final Class<T> type,
    final NexusUnhandler<? super T> unhandler)
    throws ConfigurationException;
  
  public abstract <T> void remove(
    final Class<T> type,
    final NexusUnhandler<? super T> unhandler);


  public abstract void produces(final Class<?>... types);
  
  // Calling getEmitter(type) implies produces(type)
  public abstract <T> NexusEmitter<T> getEmitter(final Class<T> type);
  
  public abstract <T> NexusHandledMarker<T> getMarker(final Class<T> type);
  
  public abstract void connect(final Object obj);
}
