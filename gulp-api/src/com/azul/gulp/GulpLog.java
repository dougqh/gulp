package com.azul.gulp;

public interface GulpLog {
  public abstract GulpLog prefetch();
  
  public abstract <T> Result<T> get(final Class<T> analysis);
  
  @SuppressWarnings("rawtypes")
  public Result process(final GenericProcessor processor);
  
  @SuppressWarnings("rawtypes")
  public Result process(final LogProcessor processor);
  
  @SuppressWarnings("rawtypes")
  public Result process(final LogProcessor.Provider processor);
  
  public <R> Result<R> analyze(final PackagedAnalyzer<R> analyzer);
  
  public <R> Result<R> analyze(final GenericAnalyzer<R> analyzer);
  
  @Deprecated
  public <R> R analyzeAndGet(final GenericAnalyzer<R> analyzer);
  
  public default <T> void forEach(
    final Class<? extends T> dataClass,
    final Processor<? super T> processor)
  {
    this.select(dataClass).forEach(processor);
  }
  
  public <T> GulpLogStream<T> select(final Class<? extends T> dataClass);
  
  public <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass,
    final Predicate<? super Pair<? super F, ? super S>> predicateFn);
  
  public <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass,
    final PairPredicate<? super F, ? super S> predicateFn);
  
  public <F, S, K> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final ThrowingFunction<? super F, ? extends K> firstKeyFn,
    final Class<? extends S> secondDataClass,
    final ThrowingFunction<? super S, ? extends K> secondKeyFn);

  public <F, S> GulpPairStream<F, S> join(
    final Class<? extends F> firstDataClass,
    final Class<? extends S> secondDataClass);
  
  public <T> GulpLog normalize(
    final Class<T> type,
    final Normalizer<T> normalizer);
  
  public <T, V> GulpLog enrich(
    final Class<T> inputType,
    final Class<V> enrichmentType,
    final Enricher<? super T, ? extends V> enricher);
  
  public GulpLog preprocess(GenericProcessor preprocessor);
}
