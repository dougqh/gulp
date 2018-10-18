package com.azul.gulp;

import java.util.Map;

public interface GulpPairStream<F, S>
  extends GulpStreamExtension<GulpPairStream<F, S>, Pair<F, S>>
{
  public GulpPairStream<F, S> filter(PairPredicate<? super F, ? super S> predicate);
  
  public GulpPairStream<F, S> filterFirst(Predicate<? super F> firstPredicate);
  
  public GulpPairStream<F, S> filterSecond(Predicate<? super S> secondPredicate);
  
  public <R> GulpStream<R> map(
    PairThrowingFunction<? super F, ? super S, ? extends R> mapFn);
  
  public Map<F, S> toMap();
  
  public void forEach(PairProcessor<? super F, ? super S> pairProcessor);
}
