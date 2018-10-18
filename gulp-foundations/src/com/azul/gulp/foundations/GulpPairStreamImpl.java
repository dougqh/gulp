package com.azul.gulp.foundations;

import java.util.Map;

import com.azul.gulp.GulpPairStream;
import com.azul.gulp.GulpStream;
import com.azul.gulp.Pair;
import com.azul.gulp.PairPredicate;
import com.azul.gulp.PairProcessor;
import com.azul.gulp.PairThrowingFunction;
import com.azul.gulp.Predicate;

final class GulpPairStreamImpl<F, S>
  extends GulpStreamBase<GulpPairStream<F, S>, Pair<F, S>>
  implements GulpPairStream<F, S>
{
  public GulpPairStreamImpl(final StreamCore<? extends Pair<F, S>> core) {
    super(core);
  }
  
  @Override
  protected GulpPairStream<F, S> createOffspring(StreamCore<? extends Pair<F, S>> core) {
    return new GulpPairStreamImpl<F, S>(core);
  }

  @Override
  public final GulpPairStream<F, S> filter(final PairPredicate<? super F, ? super S> predicate) {
    return this.filter(pair -> predicate.matches(pair.first, pair.second));
  }
  
  @Override
  public final GulpPairStream<F, S> filterFirst(final Predicate<? super F> firstPredicate) {
    return this.filter(pair -> firstPredicate.matches(pair.first));
  }
  
  @Override
  public final GulpPairStream<F, S> filterSecond(final Predicate<? super S> secondPredicate) {
    return this.filter(pair -> secondPredicate.matches(pair.second));
  }
  
  @Override
  public final <R> GulpStream<R> map(final PairThrowingFunction<? super F, ? super S, ? extends R> mapFn) {
    return this.map(pair -> mapFn.apply(pair.first, pair.second));
  }

  @Override
  public final void forEach(final PairProcessor<? super F, ? super S> pairProcessor) {
    this.forEach(pair -> pairProcessor.process(pair.first, pair.second));
  }
  
  @Override
  public final Map<F, S> toMap() {
    return this.toMap(pair -> pair.first, pair -> pair.second);
  }
}
