package com.azul.gulp;

@Deprecated
public final class ParameterizedResult<P, V> {
  private final ParameterizedResultProvider<? super P, ? extends V> provider;
  
  public ParameterizedResult(final ParameterizedResultProvider<? super P, ? extends V> provider) {
    this.provider = provider;
  }
  
  public final V get(P param) {
    return this.provider.get(param);
  }
  
  private final Number getNum(P param) {
    return (Number)this.provider.get(param);
  }
  
  public final Integer getInt(P param) {
    return this.getNum(param).intValue();
  }
  
  public final Long getLong(P param) {
    return this.getNum(param).longValue();
  }
  
  public final Double getDouble(P param) {
    return this.getNum(param).doubleValue();
  }
  
  public final void print(P param) {
    this.getResult(param).print();
  }
  
  @SafeVarargs
  public final void print(P... params) {
    for ( P param: params) {
      this.print(param);
    }
  }
  
  public final Result<V> getResult(P param) {
    return new Result<V>(param, this.get(param));
  }
}
