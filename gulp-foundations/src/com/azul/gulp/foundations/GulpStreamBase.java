package com.azul.gulp.foundations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.azul.gulp.Analyzer;
import com.azul.gulp.GroupBuilder;
import com.azul.gulp.GroupingProcessor;
import com.azul.gulp.Groups;
import com.azul.gulp.GulpPairStream;
import com.azul.gulp.GulpStream;
import com.azul.gulp.GulpStreamExtension;
import com.azul.gulp.IndexedProcessor;
import com.azul.gulp.Pair;
import com.azul.gulp.PairIterable;
import com.azul.gulp.ParameterizedAnalyzer;
import com.azul.gulp.ParameterizedResult;
import com.azul.gulp.Predicate;
import com.azul.gulp.Processor;
import com.azul.gulp.Range;
import com.azul.gulp.Result;
import com.azul.gulp.StreamProcessingException;
import com.azul.gulp.ThrowingFunction;
import com.azul.gulp.functional.Predicates;
import com.azul.gulp.functional.inject.InjectionAwareFunction;
import com.azul.gulp.functional.inject.InjectionAwarePredicate;
import com.azul.gulp.inject.InjectionAware;
import com.azul.gulp.inject.InjectionContext;


public class GulpStreamBase<E extends GulpStreamExtension<E, T>, T> implements GulpStreamExtension<E, T> {
  protected final StreamCore<? extends T> core;
  
  public GulpStreamBase(final StreamCore<? extends T> core) {
    this.core = core;
  }
  
  public GulpStreamBase(final GulpStream<? extends T> wrapped) {
    this.core = asCore(wrapped);
  }
  
  static final <T> StreamCore<? extends T> asCore(final GulpStream<? extends T> wrapped) {
    if ( wrapped instanceof GulpStreamBase ) {
      @SuppressWarnings("unchecked")
      GulpStreamBase<?, ? extends T> wrappedBase = (GulpStreamBase<?, ? extends T>)wrapped;
      return wrappedBase.core;
    } else {
      return new StreamCore<T>() {
        @Override
        protected void processImpl(Processor<? super T> processor) {
          wrapped.process(processor);
        }
      };
    }
  }
  
  @Override
  public Result process(final Processor<? super T> processor) {
    this.core.process(processor);
    return new Result(processor);
  }
  
  @Override
  public void forEach(final Processor<? super T> processor) {
    this.core.process(processor);
  }
  
  @Override
  public void forEachIndexed(final IndexedProcessor<? super T> indexedProcessor) {
    this.core.process(new Processor<T>() {
      int index = 0;
      
      @Override
      public final void process(final T object) throws Exception {
        indexedProcessor.process(this.index, object);
        
        this.index += 1;
      }
    });
  }
  
  @Override
  public final GulpStream<T> beforeInclusive(Predicate<? super T> predicate) {
    return this.before(predicate, true);
  }
  
  @Override
  public final GulpStream<T> beforeExclusive(Predicate<? super T> predicate) {
    return this.before(predicate, false);
  }
  
  @Override
  public final GulpStream<T> before(
    final Predicate<? super T> predicate,
    final boolean inclusive)
  {
    final class BeforePredicateImpl implements InjectionAwarePredicate<T> {
      private boolean matched = false;
      
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(predicate);
      }
      
      @Override
      public boolean matches(T value) {
        if ( this.matched ) return false;
        
        boolean curMatched = predicate.matches(value);
        if ( curMatched ) {
          this.matched = true;
          return inclusive;
        }
        
        return true;
      }
    }
    
    return this.filter(new BeforePredicateImpl());
  }
  
  @Override
  public final GulpStream<T> afterInclusive(Predicate<? super T> predicate) {
    return this.after(predicate, true);
  }
  
  @Override
  public final GulpStream<T> afterExclusive(Predicate<? super T> predicate) {
    return this.after(predicate, false);
  }
  
  @Override
  public final GulpStream<T> after(
    final Predicate<? super T> predicate,
    final boolean inclusive)
  {
    final class AfterPredicateImpl implements InjectionAwarePredicate<T> {
      private boolean matched = false;
      
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(predicate);
      }
      
      @Override
      public boolean matches(T value) {
        if ( this.matched ) return true;
        
        boolean curMatched = predicate.matches(value);
        if ( curMatched ) {
          this.matched = true;
          return inclusive;
        }
        
        return false;
      }
    }
    
    return this.filter(new AfterPredicateImpl());
  }
  
  @Override
  public final GulpStream<T> skip(final int count) {
    return this.filterOut(new Predicate<T>() {
      int seen = 0;
      
      @Override
      public final boolean matches(final T value) {
        this.seen += 1;
        
        return (this.seen <= count);
      }
    });
  }
  
  @Override
  public final GulpStream<T> limit(final int count) {
    return this.filter(new Predicate<T>() {
      int seen = 0;
      
      @Override
      public final boolean matches(final T value) {
        this.seen += 1;
        
        return (this.seen <= count);
      }
    });
  }
  
  @Override
  public <R> Result<R> extractRange(
    final RangeFactory<? super T, ? extends R> rangeFactory)
  {
    // TODO: Inject the rangeFactory???
    // For consistency, I don't how that would be useful.
    return this.analyze(new Analyzer<T, R>() {
      private T min = null;
      private T max = null;
    
      @Override
      public final void process(final T object) throws Exception {
        if ( this.min == null ) {
          this.min = object;
          this.max = object;
        } else {        
          int minCmp = Objects.compare(object, this.min, GenericComparator.INSTANCE);
          if ( minCmp == -1 ) this.min = object;
          
          int maxCmp = Objects.compare(object, this.max, GenericComparator.INSTANCE);
          if ( maxCmp == 1 ) this.max = object;
        }
      }
      
      @Override
      public final R result() {
        if ( this.min == null ) {
          return null;
        } else {
          return rangeFactory.make(this.min, this.max);
        }
      }
    });
  }
  
  @Override
  public <K> Groups<K, T> group(GroupingProcessor<K, T> groupingProcessor) {
    class InjectableGroupingAnalyzer implements Analyzer<T, Groups<K, T>>, InjectionAware {
      private final GroupBuilder<K, T> groupBuilder = new GroupBuilder<>();
      
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(groupingProcessor);
      }
      
      @Override
      public void process(T element) throws Exception {
        groupingProcessor.process(this.groupBuilder, element);
      }
      
      @Override
      public final Groups<K, T> result() {
        return this.groupBuilder.result();
      }
    }
    
    return this.analyze(new InjectableGroupingAnalyzer()).get();
  }
  
  @Override
  public <K> Groups<K, T> groupBy(final ThrowingFunction<? super T, ? extends K> groupFn) {
    class InjectableGroupingProcessor implements GroupingProcessor<K, T>, InjectionAware {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(groupFn);
      }
      
      @Override
      public final void process(
        final GroupBuilder<K, T> groupBuilder,
        final T element) throws Exception
      {
        K groupKey = groupFn.apply(element);
        groupBuilder.add(groupKey, element);
      }
    }
    
    return this.group(new InjectableGroupingProcessor());
  }
  
  @Override
  public final Groups<Integer, T> groupEvery(final int count) {
    return this.group(new GroupingProcessor<Integer, T>() {
      private int groupCount = 0;
      private int groupNum = 0;
      
      @Override
      public final void process(
        final GroupBuilder<Integer, T> groupBuilder,
        final T element) throws Exception
      {
        this.groupCount += 1;
        if ( this.groupCount == count ) {
          this.groupNum += 1;
          this.groupCount = 1;
        }
        
        groupBuilder.add(groupNum, element);
      }
    });
  }
  
  @Override
  public final Groups<Integer, T> splitOn(final Predicate<? super T> predicateFn) {
    class InjectableGroupingProcessor implements GroupingProcessor<Integer, T>, InjectionAware {
      Integer groupNum = null;
      
      @Override
      public final void onInject(InjectionContext ctx) {
        ctx.inject(predicateFn);
      }
      
      @Override
      public final void process(
        final GroupBuilder<Integer, T> groupBuilder,
        final T element) throws Exception
      {
        if ( this.groupNum == null ) {
          this.groupNum = 1;
        } else if ( predicateFn.matches(element) ) {
          this.groupNum += 1;
        }

        groupBuilder.add(this.groupNum, element);
      }
    }
    
    return this.group(new InjectableGroupingProcessor());
  }
  
  @Override
  public final Groups<Integer, T> splitOn(final T value) {
    return this.splitOn(e -> Objects.equals(e, value));
  }
  
  @Override
  public <V> Result<Range<V>> range(
    final ThrowingFunction<? super T, ? extends V> lowerFn,
    final ThrowingFunction<? super T, ? extends V> upperFn)
  {
    // DQH - Kind of awful flatMap into single Iterable<V> -- and then use normal range
    class InjectableMapper implements ThrowingFunction<T, Iterable<V>>, InjectionAware {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(lowerFn);
        ctx.inject(upperFn);
      }
      
      @Override
      public Iterable<V> apply(T input) throws Exception {
        return Arrays.asList(lowerFn.apply(input), upperFn.apply(input));
      }
    }
    
    return this.flatMap(new InjectableMapper()).range();
  }
  
  @Override
  public Result<Range<T>> range() {
    return this.extractRange(Range::make);
  }
  
  @Override
  public <V> Result<Range<V>> range(
    final ThrowingFunction<? super T, ? extends V> mapFn)
  {
    GulpStream<V> mapped = this.map(mapFn);
    return mapped.range();
  }
  
  @Override
  public final Result<T> min() {
    return this.range().map(range -> range == null ? null : range.start);
  }
  
  @Override
  public <V> Result<V> min(ThrowingFunction<? super T, ? extends V> mapFn) {
    GulpStream<V> mapped = this.map(mapFn);
    return mapped.min();
  }
  
  @Override
  public final Result<T> max() {
    return this.range().map(range -> range == null ? null : range.end);
  }
  
  @Override
  public <V> Result<V> max(ThrowingFunction<? super T, ? extends V> mapFn) {
    GulpStream<V> mapped = this.map(mapFn);
    return mapped.max();
  }
  
  @Override
  public <C extends Comparable<C>> GulpStream<T> sortBy(
    final ThrowingFunction<? super T, ? extends C> mapFn)
  {
    return this.sortByImpl(mapFn, false);
  }
  
  @Override
  public <C extends Comparable<C>> GulpStream<T> sortByDescending(
    ThrowingFunction<? super T, ? extends C> mapFn)
  {
    return this.sortByImpl(mapFn, true);
  }
  
  private <C extends Comparable<C>> GulpStream<T> sortByImpl(
    final ThrowingFunction<? super T, ? extends C> mapFn,
    final boolean invert)
  {
    // TODO: DQH - Yes, this is awful.
    // It is a named method scoped class that captures parameters.
    // It is done this way because an anonymous inner cannot 
    // implement the extra InjectionAware interface that's needed 
    // for the injection magic to work.
    class InjectableMappingComparator implements Comparator<T>, InjectionAware {
      @Override
      public final void onInject(final InjectionContext ctx) {
        ctx.inject(mapFn);
      }
      
      @Override
      public final int compare(final T lhs, final T rhs) {
        try {
          C mappedLhs = mapFn.apply(lhs);
          C mappedRhs = mapFn.apply(rhs);
          
          return mappedLhs.compareTo(mappedRhs);
        } catch ( Exception e ) {
          throw new IllegalStateException(e);
        }
      }
    }
    
    return this.sortImpl(new InjectableMappingComparator(), invert);
  }
  
  @Override
  public final GulpStream<T> sort() {
    @SuppressWarnings("unchecked")
    Comparator<T> comparator = (Comparator<T>)GenericComparator.INSTANCE;
    
    return this.sortImpl(comparator, false);
  }
  
  @Override
  public GulpStream<T> sortDescending() {
    @SuppressWarnings("unchecked")
    Comparator<T> comparator = (Comparator<T>)GenericComparator.INSTANCE;
    
    return this.sortImpl(comparator, true);
  }
  
  @Override
  public final GulpStream<T> sortUsing(final Comparator<? super T> comparator) {
    return this.sortImpl(comparator, false);
  }
  
  private final GulpStream<T> sortImpl(
    final Comparator<? super T> comparator,
    final boolean invert)
  {
    final InjectionContext injectionCtx = this.core.injectionContext();
    
    injectionCtx.inject(comparator);
    
    Comparator<? super T> finalComparator = invert ?
      (lhs, rhs) -> comparator.compare(rhs, lhs) :
      comparator;
    
    return new GulpStreamPlain<>(new StreamCore<T>() {
      private List<T> sorted = null;
      
      protected final InjectionContext injectionContext() {
        return injectionCtx;
      }
      
      protected void processImpl(final Processor<? super T> processor) {
        if ( this.sorted == null ) {
          this.sorted = GulpStreamBase.this.toList();
          this.sorted.sort(finalComparator);
        }

        try {
          for ( T element: this.sorted ) {
            processor.process(element);
          }
        } catch ( Exception e ) {
          throw new StreamProcessingException(e);
        }
      }
    });
  }
  
  @Override
  public <R> Result<R> analyze(Analyzer<? super T, ? extends R> analyzer)
    throws StreamProcessingException
  {
    this.core.process(analyzer);
    return new Result<R>(analyzer);
  }
  
  @Override
  public <R> R analyzeAndGet(Analyzer<? super T, ? extends R> analyzer)
    throws StreamProcessingException
  {
    return this.analyze(analyzer).get();
  }
  
  @Override
  public <P, R> ParameterizedResult<P, R> analyze(
    final ParameterizedAnalyzer<? super T, ? super P, ? extends R> analyzer)
    throws StreamProcessingException
  {
    this.core.process(analyzer);
    return new ParameterizedResult<P, R>(analyzer);
  }
  
  @Override
  public boolean matchesOne(final Predicate<? super T> predicate) {
    return (this.first(predicate) != null);
  }
  
  @Override
  public boolean matchesOne(final Set<? super T> set) {
    return (this.first(set) != null);
  }
  
  @Override
  public Optional<T> first() {
    return this.analyze(new Analyzer<T, Optional<T>>() {
      T first = null;
      
      @Override
      public final void process(T object) {
        if ( this.first == null ) {
          this.first = object;
        }
      }
      
      @Override
      public final Optional<T> result() {
        return Optional.ofNullable(this.first);
      }
    }).get();
  }
  
  @Override
  public Optional<T> first(final Predicate<? super T> predicate) {
    return this.filter(predicate).first();
  }
  
  @Override
  public Optional<T> first(Set<? super T> set) {
    return this.first(set::contains);
  }
  
  @Override
  public Optional<T> last() {
    return this.analyze(new Analyzer<T, Optional<T>>() {
      T last = null;
      
      @Override
      public final void process(final T object) {
        this.last = object;
      }
      
      @Override
      public final Optional<T> result() {
        return Optional.ofNullable(this.last);
      }
    }).get();
  }
  
  @Override
  public Optional<T> last(Predicate<? super T> predicate) {
    return this.filter(predicate).last();
  }
  
  @Override
  public Optional<T> last(Set<? super T> set) {
    return this.last(set::contains);
  }
  
  protected E createOffspring(final StreamCore<? extends T> core) {
    @SuppressWarnings("unchecked")
    E casted = (E)new GulpStreamBase<E, T>(core);
    return casted;
  }
  
  protected final <U> GulpStream<U> createDerivative(final StreamCore<? extends U> core) {
    // should be GulpStreamBase<E, U> but javac emits an improper compilation error
    return new GulpStreamBase(core);
  }
    
  protected final <F, S> GulpPairStream<F, S> createPairDerivative(
    final StreamCore<? extends Pair<F, S>> core)
  {
    return new GulpPairStreamImpl<F, S>(core);
  }
  
  @Override
  public boolean contains(T element) {
    return ( this.filter(element).count().get() != 0 );
  }
  
  @Override
  public boolean contains(Predicate<? super T> predicate) {
    return ( this.filter(predicate).count().get() != 0 );
  }
  
  @Override
  public E filter(final Predicate<? super T> predicate) {
    return this.createOffspring(this.core.filter(predicate));
  }
  
  @Override
  public E filter(final Set<? super T> set) {
    return this.filter(set::contains);
  }
  
  @Override
  public final E filter(final T... ts) {
    Set<T> set = new HashSet<T>(ts.length);
    set.addAll(Arrays.asList(ts));
    
    return this.filter(set);
  }
  
  @Override
  public E filterOut(Predicate<? super T> predicate) {
    return this.filter(Predicates.not(predicate));
  }
  
  @Override
  public final E filterOut(Set<? super T> set) {
    return this.filterOut(set::contains);
  }
  
  @Override
  public final E filterOut(final T... ts) {
    Set<T> set = new HashSet<T>(ts.length);
    set.addAll(Arrays.asList(ts));
    
    return this.filterOut(set);
  }
  
  @Override
  public E unique() {
    return this.filter(Predicates.unique());
  }
  
  @Override
  public E unique(final ThrowingFunction<? super T, ?> identityFn) {
    return this.filter(Predicates.unique(identityFn));
  }
  
  @Override
  public Result<Integer> count() {
    return this.analyze(new Analyzer<T, Integer>() {
      int count = 0;
      
      @Override
      public final void process(T object) { 
        this.count += 1;
      }
      
      @Override
      public final Integer result() {
        return this.count;
      }
    });
  }

  public <U> GulpStream<U> flatMap(final ThrowingFunction<? super T, ? extends Iterable<? extends U>> transform) {
    return this.createDerivative(this.core.flatMap(transform));
  }
  
  @Override
  public <U, V> GulpPairStream<U, V> flatMap(
    final ThrowingFunction<? super T, ? extends Iterable<? extends U>> firstMap,
    final ThrowingFunction<? super T, ? extends Iterable<? extends V>> secondMap)
  {
    InjectionAwareFunction<T, Iterable<Pair<U, V>>> pairIterableFn = new InjectionAwareFunction<T, Iterable<Pair<U, V>>>() {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(firstMap);
        ctx.inject(secondMap);
      }

      @Override
      public Iterable<Pair<U, V>> apply(T input) throws Exception {
        return PairIterable.make(firstMap.apply(input), secondMap.apply(input));
      }
    };
    
    return this.createPairDerivative(this.core.flatMap(pairIterableFn));
  }
  
  @Override
  public <U> GulpStream<U> map(final ThrowingFunction<? super T, ? extends U> transform) {
    return this.createDerivative(this.core.map(transform));
  }
  
  @Override
  public <U> GulpStream<U> map(final Map<? super T, ? extends U> map) {
    return this.map((t) -> map.get(t));
  }
  
  @Override
  public <U, V> GulpPairStream<U, V> map(
    final ThrowingFunction<? super T, ? extends U> firstMap,
    final ThrowingFunction<? super T, ? extends V> secondMap)
  {
    InjectionAwareFunction<T, Pair<U, V>> pairFn = new InjectionAwareFunction<T, Pair<U, V>>() {
      @Override
      public void onInject(InjectionContext ctx) {
        ctx.inject(firstMap);
        ctx.inject(secondMap);
      }

      @Override
      public Pair<U, V> apply(T input) throws Exception {
        return Pair.make(firstMap.apply(input), secondMap.apply(input));
      }
    };
    
    return this.createPairDerivative(this.core.map(pairFn));
  }
  
  @Override
  public GulpStream<Boolean> predicateMap(final Predicate<? super T> predicate) {
    return this.map(Predicates.asFunction(predicate));
  }
  
  public final void addTo(final Collection<? super T> collection) {
    this.process(collection::add);
  }
  
  @Override
  public final List<T> toList(final Predicate<? super T> predicate) {
    return this.filter(predicate).toList();
  }
  
  @Override
  public final List<T> toList() {
    ArrayList<T> list = new ArrayList<>();
    this.process(list::add);
    return list;
  }
  
  @Override
  public final List<T> toList(final int maxElements) {
    // TODO: Implement in a more memory efficient fashion
    List<T> list = this.toList();
    return list.subList(0, Math.min(maxElements, list.size()));
  }
  
  @Override
  public final Set<T> toSet(final Predicate<? super T> predicate) {
    return this.filter(predicate).toSet();
  }
  
  @Override
  public final Set<T> toSet() {
    HashSet<T> set = new HashSet<>();
    this.process(set::add);
    return set;
  }
  
  @Override
  public final SortedSet<T> toSortedSet(Predicate<? super T> predicate) {
    return this.filter(predicate).toSortedSet();
  }
  
  @Override
  public final SortedSet<T> toSortedSet() {
    TreeSet<T> set = new TreeSet<>();
    this.process(set::add);
    return set;
  }
  
  @Override
  public final <K> Map<K, T> toMap(final ThrowingFunction<? super T, ? extends K> keyFn) {
    HashMap<K, T> map = new HashMap<>();
    this.process(e -> {
      map.put(keyFn.apply(e), e);
    });
    return map;
  }
  
  @Override
  public <K, V> Map<K, V> toMap(
    final ThrowingFunction<? super T, ? extends K> keyFn,
    final ThrowingFunction<? super T, ? extends V> valueFn)
  {
    HashMap<K, V> map = new HashMap<>();
    this.process(e -> {
      map.put(keyFn.apply(e), valueFn.apply(e));
    });
    return map;
  }
  
  @Override
  public <K extends Comparable<?>> SortedMap<K, T> toSortedMap(ThrowingFunction<? super T, ? extends K> keyFn) {
    TreeMap<K, T> map = new TreeMap<>();
    this.process(e -> {
      map.put(keyFn.apply(e), e);
    });
    return map;
  }
  
  @Override
  public <K, V> SortedMap<K, V> toSortedMap(
    ThrowingFunction<? super T, ? extends K> keyFn,
    ThrowingFunction<? super T, ? extends V> valueFn)
  {
    TreeMap<K, V> map = new TreeMap<>();
    this.process(e -> {
      map.put(keyFn.apply(e), valueFn.apply(e));
    });
    return map;
  }
  
  @Override
  public final void print() {
    this.printTo(System.out);
  }
  
  @Override
  public final void printTo(PrintStream out) {
    this.process(out::println);
    out.println();
  }
  
  @Override
  public final void printTo(final OutputStream out) {
    this.printTo(new PrintStream(out, true));
  }
  
  @Override
  public final void printTo(final Writer writer) {
    this.process(obj -> {
      writer.write(obj.toString());
      writer.write('\n');
    });
    try {
      writer.write('\n');
    } catch ( IOException e ) {
      throw new IllegalStateException(e);
    }
  }
  
  @Override
  public void print(ThrowingFunction<? super T, ? extends String> fn) {
    this.map(fn).print();
  }
  
  private static final class GenericComparator implements Comparator<Object> {
    private static final GenericComparator INSTANCE = new GenericComparator();
    
    @Override
    public final int compare(final Object lhs, final Object rhs) {
      if ( lhs instanceof Comparable && rhs instanceof Comparable ) {
        Comparable lhsComparable = (Comparable)lhs;
        Comparable rhsComparable = (Comparable)rhs;
        
        return lhsComparable.compareTo(rhsComparable);
      } else {
        throw new IllegalArgumentException();
      }
    }
  }
}
