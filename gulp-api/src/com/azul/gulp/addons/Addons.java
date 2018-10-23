package com.azul.gulp.addons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Addons {
  public static abstract class Addon<T> {
	public final Class<T> targetClass() {
      @SuppressWarnings({"unchecked", "rawtypes"})
	  Class<? extends Addon<T>> addonClass = (Class)this.getClass();
      return targetClassOf(addonClass);
	}
	
    public static final <T> Class<T> targetClassOf(final Class<? extends Addon<T>> addonClass) {
	  Type superType = addonClass.getGenericSuperclass();
	  ParameterizedType paramType = (ParameterizedType)superType;
	  
	  @SuppressWarnings("unchecked")
	  Class<T> targetKlass = (Class<T>)paramType.getActualTypeArguments()[0];
	  return targetKlass;
    }
  }
  
  private volatile List<Class<Addon<?>>> cachedAddons;
  
  public final List<Class<Addon<?>>> loadAddons() {
	List<Class<Addon<?>>> cachedAddons = this.cachedAddons;
	if ( cachedAddons != null ) return cachedAddons;
	
	List<Class<Addon<?>>> loadedAddons = 
	  Collections.unmodifiableList(this.loadAddonsImpl());
	this.cachedAddons = loadedAddons;
	return loadedAddons;
  }
  
  private final List<Class<Addon<?>>> loadAddonsImpl() {
	Class<?>[] childClasses = this.getClass().getDeclaredClasses();
	  
	List<Class<Addon<?>>> addons = new ArrayList<>();
	for ( Class<?> childClass: this.loadAddons()) {
	  boolean isAddon = childClass.isAssignableFrom(Addon.class);
	  if ( !isAddon ) continue;
	  
	  @SuppressWarnings("unchecked")
	  Class<Addon<?>> addonClass = (Class<Addon<?>>)childClass;
	  addons.add(addonClass);
	}
	return addons;
  }
  
  @SuppressWarnings("rawtypes")
  private static final Class<?> targetClassOf(final Class<? extends Addon> addonClass) {
	Type superType = addonClass.getGenericSuperclass();
	ParameterizedType paramType = (ParameterizedType)superType;
	  
	Class<?> targetKlass = (Class<?>)paramType.getActualTypeArguments()[0];
	return targetKlass;
  }
}
