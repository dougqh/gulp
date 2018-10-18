package com.azul.gulp;

public final class Result<T> {
  private final Object label;
  private final Object processor;
  
  public Result(final Object label, final Object result) {
    this.label = label;
    this.processor = result;
  }
  
  public Result(final Object processor) {
    this.label = null;
    this.processor = processor;
  }
  
  public <T2 extends T> T2 get() {
    @SuppressWarnings("unchecked")
    T2 casted = (T2)this.getImpl();
    return casted;
  }
  
  private Object getImpl() {
    if ( this.processor instanceof ResultProvider ) {
      ResultProvider<?> resultProvider = (ResultProvider<?>)this.processor;
      return resultProvider.result();
    } else {
      return this.processor;
    }
  }
  
  private final Number numValue() {
    return (Number)this.getImpl();
  }
  
  public final int intValue() {
    return this.numValue().intValue();
  }
  
  public final long longValue() {
    return this.numValue().longValue();
  }
  
  public final double doubleValue() {
    return this.numValue().doubleValue();
  }
  
  public <U> Result<U> map(final ThrowingFunction<? super T, ? extends U> mapFn) {
    try {
      return new Result<U>(mapFn.apply(this.get()));
    } catch ( Exception e ) {
      throw new IllegalStateException(e);
    }
  }
  
  public final void print() {
    System.out.println(this);
  }
  
  public final void print(final String heading) {
    System.out.println(heading);
    this.print();
  }
  
  @Override
  public final String toString() {
    if ( this.label != null ) {
      return this.label + ":\t" + this.get().toString();
    } else {
      return this.get().toString();
    }
  }
}
