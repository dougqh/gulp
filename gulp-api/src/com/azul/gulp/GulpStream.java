package com.azul.gulp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;



public interface GulpStream<T> /* TODO implements Stream */ {
  public static interface RangeFactory<C, R> {
    public R make(C start, C end);
  }
  
  
  public abstract <U> GulpStream<U> map(ThrowingFunction<? super T, ? extends U> transform);
  
  public abstract <U> GulpStream<U> map(final Map<? super T, ? extends U> map);
  
  public abstract <U, V> GulpPairStream<U, V> map(
    final ThrowingFunction<? super T, ? extends U> firstMap,
    final ThrowingFunction<? super T, ? extends V> secondMap);
  
  public abstract <U> GulpStream<U> flatMap(ThrowingFunction<? super T, ? extends Iterable<? extends U>> transform);
  
  public abstract <U, V> GulpPairStream<U, V> flatMap(
    final ThrowingFunction<? super T, ? extends Iterable<? extends U>> firstMap,
    final ThrowingFunction<? super T, ? extends Iterable<? extends V>> secondMap);
  
  public abstract GulpStream<Boolean> predicateMap(Predicate<? super T> predicate);
  
  public abstract boolean contains(T element);
  
  public abstract boolean contains(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> beforeInclusive(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> beforeExclusive(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> before(
    Predicate<? super T> predicate,
    boolean inclusive);
  
  public abstract GulpStream<T> afterInclusive(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> afterExclusive(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> after(
    Predicate<? super T> predicate,
    boolean inclusive);
  
  public abstract GulpStream<T> skip(int count);
  
  public abstract GulpStream<T> limit(int count);
  
  public abstract GulpStream<T> filter(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> filter(Set<? super T> set);
  
  @SuppressWarnings("unchecked")
  public abstract GulpStream<T> filter(T... set);
  
  public abstract GulpStream<T> filterOut(Predicate<? super T> predicate);
  
  public abstract GulpStream<T> filterOut(Set<? super T> set);
  
  @SuppressWarnings("unchecked")
  public abstract GulpStream<T> filterOut(T... set);
  
  public abstract GulpStream<T> sort();
  
  public abstract GulpStream<T> sortDescending();
  
  public abstract <C extends Comparable<C>> GulpStream<T> sortBy(
    final ThrowingFunction<? super T, ? extends C> mapFn);
  
  public abstract <C extends Comparable<C>> GulpStream<T> sortByDescending(
      final ThrowingFunction<? super T, ? extends C> mapFn);
  
  public abstract GulpStream<T> sortUsing(final Comparator<? super T> comparator);

  public abstract GulpStream<T> unique();
  
  public abstract GulpStream<T> unique(final ThrowingFunction<? super T, ?> identityFn);
  
  // currently hard to implement - need to think about to this.
  // public abstract <K> GulpStream<T> lastUnique(final ThrowingFunction<? super T, K> identityFn);
  
  public abstract Result<Integer> count();
  
  // public abstract <K> Result<Counts<K>> countBy(final ThrowingFunction<? super T, ? extends K> keyFn);
  
  @SuppressWarnings("rawtypes")
  public abstract Result process(Processor<? super T> processor)
    throws StreamProcessingException;
  
  public abstract void forEachIndexed(IndexedProcessor<? super T> processor)
    throws StreamProcessingException;
  
  public abstract void forEach(Processor<? super T> processor)
    throws StreamProcessingException;
  
  // public abstract void forEachPacket(Processor<Packet<? super T>> processor);
 
  public abstract <R> Result<R> analyze(Analyzer<? super T, ? extends R> analyzer)
    throws StreamProcessingException;
  
  public abstract <R> R analyzeAndGet(Analyzer<? super T, ? extends R> analyzer)
    throws StreamProcessingException;
  
  public abstract <P, R> ParameterizedResult<P, R> analyze(ParameterizedAnalyzer<? super T, ? super P, ? extends R> analyzer)
    throws StreamProcessingException;
  
  public abstract boolean matchesOne(final Predicate<? super T> predicate);
  
  public abstract boolean matchesOne(final Set<? super T> set);
  
  public abstract Optional<T> first(Predicate<? super T> predicate);
  
  public abstract Optional<T> first(Set<? super T> set);
  
  public abstract Optional<T> first();
  
  public abstract Optional<T> last(Predicate<? super T> predicate);
  
  public abstract Optional<T> last(Set<? super T> predicate);
  
  public abstract Optional<T> last();
  
  public abstract <K> Groups<K, T> group(GroupingProcessor<K, T> groupingProcessor);
  
  public abstract <K> Groups<K, T> groupBy(ThrowingFunction<? super T, ? extends K> groupFn);
  
  public abstract Groups<Integer, T> groupEvery(int count);
  
  public abstract Groups<Integer, T> splitOn(final T value);
  
  public abstract Groups<Integer, T> splitOn(Predicate<? super T> predicateFn);
  
  public abstract Result<T> min();
  
  public abstract <V> Result<V> min(
    final ThrowingFunction<? super T, ? extends V> mapFn);
  
  public abstract Result<T> max();
  
  public abstract <V> Result<V> max(
   final ThrowingFunction<? super T, ? extends V> mapFn);
  
  public abstract Result<Range<T>> range();
  
  public abstract <V> Result<Range<V>> range(
    final ThrowingFunction<? super T, ? extends V> mapFn);
  
  public abstract <V> Result<Range<V>> range(
    final ThrowingFunction<? super T, ? extends V> lowerFn,
    final ThrowingFunction<? super T, ? extends V> upperFn);
  
  public abstract <R> Result<R> extractRange(RangeFactory<? super T, ? extends R> rangeFactory);
  
  public abstract void addTo(Collection<? super T> collection);
  
  public abstract Set<T> toSet(Predicate<? super T> predicate);
  
  public abstract Set<T> toSet();
  
  public abstract SortedSet<T> toSortedSet(Predicate<? super T> predicate);
  
  public abstract SortedSet<T> toSortedSet();
  
  public abstract List<T> toList(Predicate<? super T> predicate);
  
  public abstract List<T> toList();
  
  public abstract List<T> toList(int maxElements);
  
  public abstract <K> Map<K, T> toMap(ThrowingFunction<? super T, ? extends K> keyFn);
  
  public abstract <K, V> Map<K, V> toMap(
    ThrowingFunction<? super T, ? extends K> keyFn,
    ThrowingFunction<? super T, ? extends V> valueFn);
  
  public abstract <K extends Comparable<?>> SortedMap<K, T> toSortedMap(ThrowingFunction<? super T, ? extends K> keyFn);
  
  public abstract <K, V> SortedMap<K, V> toSortedMap(
     ThrowingFunction<? super T, ? extends K> keyFn,
     ThrowingFunction<? super T, ? extends V> valueFn);
  
  public abstract void print();
  
  public abstract void printTo(OutputStream out);
  
  public abstract void printTo(PrintStream out);
  
  public abstract void printTo(Writer writer);
  
  public abstract void print(ThrowingFunction<? super T, ? extends String> fn);
}
