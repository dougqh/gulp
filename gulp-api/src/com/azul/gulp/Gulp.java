package com.azul.gulp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class Gulp {
  private Gulp() {}
  
  // Used to annotate an event processing method on a GenericProcessor
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Process {
    boolean optional() default false;
  }
  
  // Used to annotate an unhandled event processing method on a GenericProcessor
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ProcessUnhandled {}
  
  // Used to inject non-event helper objects into any non-event object
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
  public @interface Inject {}
  
  // Used to capture prior event objects of given type into any object 
  // including other event objects
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public @interface Capture {
    // TODO: Support predication
    // public Class<? extends Predicate> predicate() default Predicate.class;
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public @interface Key {
    public abstract Class<?> value() default Object.class;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public @interface ForeignKey {
    public abstract Class<?> value();
  }
  
//  public @interface Predicate {
//    public abstract Class<PredicatedClass<?>> value();
//  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Result {}
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ComplexEvent {
    public Class<? extends GenericProcessor> value();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface Normalize {
    public Class<? extends Normalizer<?>> value();
  }
}
