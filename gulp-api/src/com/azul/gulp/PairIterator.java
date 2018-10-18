package com.azul.gulp;

import java.util.Iterator;

public final class PairIterator<F, S> implements Iterator<Pair<F, S>> {
  public static final <F, S> PairIterator<F, S> make(
    final Iterator<? extends F> firstIterator,
    final Iterator<? extends S> secondIterator)
  {
    return new PairIterator<F, S>(firstIterator, secondIterator);
  }
  
  private final Iterator<? extends F> firstIterator;
  private final Iterator<? extends S> secondIterator;
  
  public PairIterator(
    final Iterator<? extends F> firstIterator,
    final Iterator<? extends S> secondIterator)
  {
    this.firstIterator = firstIterator;
    this.secondIterator = secondIterator;
  }
  
  @Override
  public final boolean hasNext() {
    return this.firstIterator.hasNext() && this.secondIterator.hasNext();
  }
  
  @Override
  public final Pair<F, S> next() {
    return Pair.make(this.firstIterator.next(), this.secondIterator.next());
  }
}
