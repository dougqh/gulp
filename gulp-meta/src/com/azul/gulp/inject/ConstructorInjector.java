package com.azul.gulp.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.azul.gulp.Gulp;


public class ConstructorInjector<T> implements ExactInjector {
  private final Constructor<T> injectableCtor;
  private final Object[] params;
  
  public ConstructorInjector(final Class<T> type) {
    this.injectableCtor = getInjectableCtor(type);
    this.params = new Object[this.injectableCtor.getParameterTypes().length];
  }
  
  private static final <T> Constructor<T> getInjectableCtor(final Class<T> type) {
    Constructor<?>[] ctors = type.getDeclaredConstructors();
    
    Constructor<?> injectableCtor = null;
    
    for ( Constructor<?> ctor: ctors ) {
      if ( ctor.isAnnotationPresent(Gulp.Inject.class) ) {
        if ( injectableCtor != null ) {
          throw new IllegalStateException("Two injectable constructors!");
        }
        
        injectableCtor = ctor;
        injectableCtor.setAccessible(true);
      }
    }
    
    if ( injectableCtor != null ) {
      return cast(injectableCtor);
    }
    
    // Couldn't find an Injectable constructor -- if appropriate return the sole constructor
    if ( ctors.length != 1 ) throw new IllegalStateException("ambiguous constructor choice");
    
    Constructor<?> ctor = ctors[0];
    ctor.setAccessible(true);
    return cast(ctor);
  }
  
  private static final <T> Constructor<T> cast(final Constructor<?> ctor) {
    @SuppressWarnings("unchecked")
    Constructor<T> casted = (Constructor<T>)ctor;
    return casted;
  }
  
  @Override
  public Set<Type> requires() {
    Type[] types = this.injectableCtor.getGenericParameterTypes();
    if ( types.length == 0 ) {
      return Collections.emptySet();
    } else {
      Set<Type> typeSet = new HashSet<>(types.length);
      typeSet.addAll(Arrays.asList(types));
      
      return Collections.unmodifiableSet(typeSet);
    }
  }
  
  public void inject(Type type, Object value) {
    Type[] types = this.injectableCtor.getGenericParameterTypes();
    for ( int i = 0; i < types.length; ++i ) {
      if ( types[i].equals(type) ) {
        this.params[i] = value;
      }
    }
  }
  
  public final T make() {
    try {
      return this.injectableCtor.newInstance(this.params);
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException(e);
    }
  }
}
