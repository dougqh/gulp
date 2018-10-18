package com.azul.gulp;

import java.util.Objects;

public final class Pair<F, S> {
  public static final <F, S> Pair<F, S> make(final F first, final S second) {
    return new Pair<F, S>(first, second);
  }
  
  public final F first;
  public final S second;
  
  public Pair(final F first, final S second) {
    this.first = first;
    this.second = second;
  }
  
  public final F first() { return this.first; }
  public final S second() { return this.second; }
  
  public final int hashCode() {
    return Objects.hash(this.first, this.second);
  }
  
  @Override
  public boolean equals(Object obj) {
    if ( !(obj instanceof Pair) ) return false;
    
    Pair<?, ?> that = (Pair<?, ?>)obj;
    return this.first.equals(that.first) && this.second.equals(that.second);
  }
  
  @Override
  public String toString() {
    return String.format("<%s - %s>", this.first, this.second);
  }
}
