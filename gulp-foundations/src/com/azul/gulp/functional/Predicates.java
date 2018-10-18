package com.azul.gulp.functional;

import java.util.HashSet;
import java.util.Set;

import com.azul.gulp.Predicate;
import com.azul.gulp.ThrowingFunction;
import com.azul.gulp.functional.inject.InjectionAwareFunction;
import com.azul.gulp.functional.inject.InjectionAwarePredicate;
import com.azul.gulp.inject.InjectionContext;

public final class Predicates {
  private Predicates() {}
  
  private static final Predicate<Object> ANY = new Predicate<Object>() {
    @Override
    public final boolean matches(final Object value) {
      return true;
    }
  };
  
  private static final Predicate<Object> NOT_NULL = new Predicate<Object>() {
    @Override
    public final boolean matches(final Object value) {
      return (value != null);
    }
  };
  
  public static final boolean isAny(final Predicate<?> predicate) {
    return (predicate == Predicates.ANY);
  }
  
  public static final <T> Predicate<T> any() {
    @SuppressWarnings("unchecked")
    Predicate<T> casted = (Predicate<T>)ANY;
    return casted;
  }
  
  public static final <T> Predicate<T> not(final Predicate<? super T> predicate) {
    return new InjectionAwarePredicate<T>() {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(predicate);
      }
      
      @Override
      public final boolean matches(final T value) {
        return !predicate.matches(value);
      }
    };
  }
  
  public static final <T> Predicate<T> notNull() {
    @SuppressWarnings("unchecked")
    Predicate<T> casted = (Predicate<T>)NOT_NULL;
    return casted;
  }
  
  public static final <T> Predicate<T> and(
      final Predicate<? super T> lhs,
      final Predicate<? super T> rhs)
  {
    return new InjectionAwarePredicate<T>() {
      @Override
      public void onInject(final InjectionContext ctx) {
        ctx.inject(lhs);
        ctx.inject(rhs);
      }
      
      public boolean matches(T value) {
        return lhs.matches(value) && rhs.matches(value);
      }
    };
  }
  
  public static final <T> Predicate<T> or(
      final Predicate<? super T> lhs,
      final Predicate<? super T> rhs)
  {
    return new InjectionAwarePredicate<T>() {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(lhs);
        ctx.inject(rhs);
      }
      
      public boolean matches(T value) {
        return lhs.matches(value) || rhs.matches(value);
      }
    };
  }
  
  public static final <T> Predicate<T> unique() {
    return new Predicate<T>() {
      private final Set<T> seen = new HashSet<>(64);
      
      @Override
      public final boolean matches(final T value) {
        return this.seen.add(value);
      }
    };
  }
  
  public static final <T> Predicate<T> unique(final ThrowingFunction<? super T, ?> transformFn) {
    return new Predicate<T>() {
      private final Set<Object> seen = new HashSet<>(64);
      
      @Override
      public final boolean matches(final T value) {
        try {
          return this.seen.add(transformFn.apply(value));
        } catch ( Exception e ) {
          throw new IllegalStateException(e);
        }
      }
    };
  }
  
  public static final <T> ThrowingFunction<T, Boolean> asFunction(final Predicate<? super T> predicate) {
    return new InjectionAwareFunction<T, Boolean>() {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(predicate);
      }
      
      @Override
      public final Boolean apply(final T input) {
        return predicate.matches(input);
      }
    };
  }
}
