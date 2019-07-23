package com.azul.gulp;

import java.util.Objects;

public class Range<C> implements Comparable<Range<C>> {
  public static final <C> Range<C> make(C start, C end) {
    return new Range<C>(start, end);
  }
  

  public static final <C> Range<C> merge(Range<C> r1, Range<C> r2) {
    return make(min(r1.start, r2.start), max(r1.end, r2.end));
      
  }
  
  public final C start;
  public final C end;
  
  public Range(final C start, final C end) {
    this.start = start;
    this.end = end;
  }
  
  public final boolean contains(final C value) {
    return compare(this.start, value) <= 0 &&
        compare(this.end, value) >= 0;
  }
  
  @SuppressWarnings("unchecked")
  private static final <C> int compare(C lhs, C rhs) {
    if ( !(lhs instanceof Comparable) ) throw new UnsupportedOperationException();
    if ( !(rhs instanceof Comparable) ) throw new UnsupportedOperationException();
    
    Comparable<C> lhsComp = (Comparable<C>)lhs;
    return lhsComp.compareTo(rhs);
  }
  
  private static final <C> C min(C lhs, C rhs) {
    return compare(lhs, rhs) <= 0 ? lhs : rhs;
  }
  
  private static final <C> C max(C lhs, C rhs) {
    return compare(lhs, rhs) >= 0 ? lhs : rhs;
  }
  
  @Override
  public final int hashCode() {
    return Objects.hash(this.start, this.end);
  }
  
  @Override
  public final boolean equals(final Object obj) {
    if ( !(obj instanceof Range) ) return false;
    
    Range<?> that = (Range<?>)obj;
    return this.start.equals(that.start) &&
      this.end.equals(that.end);
  }
  
  @Override
  public int compareTo(Range<C> that) {
    int startComp = compare(this.start, that.start);
    if ( startComp != 0 ) return startComp;
    
    int endComp = compare(this.end, that.end);
    return endComp;
  }
  
  @Override
  public final String toString() {
    return "[" + this.start + ", " + this.end + "]";
  }
}
