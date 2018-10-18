package com.azul.gulp;

import java.util.Iterator;

public class PairIterable<F, S> implements Iterable<Pair<F, S>> {
  public static final <F, S> PairIterable<F, S> make(
    final Iterable<? extends F> firstIterable,
    final Iterable<? extends S> secondIterable)
  {
    return new PairIterable<F, S>(firstIterable, secondIterable);
  }
  
  private final Iterable<? extends F> firstIterable;
  private final Iterable<? extends S> secondIterable;
  
  public PairIterable(
    final Iterable<? extends F> firstIterable,
    final Iterable<? extends S> secondIterable)
  {
    this.firstIterable = firstIterable;
    this.secondIterable = secondIterable;
  }
  
  @Override
  public final Iterator<Pair<F, S>> iterator() {
    return new PairIterator<F, S>(this.firstIterable.iterator(), this.secondIterable.iterator());
  }
}
