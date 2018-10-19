package com.azul.gulp.excel;

import org.apache.poi.ss.usermodel.Sheet;

import com.azul.gulp.Processor;
import com.azul.gulp.sources.Converter;
import com.azul.gulp.sources.Source;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpRowConverter;

class ExcelRowSource extends Source<GulpRow> {
  private final Sheet sheet;
  private final int startInclusive;
  private final int endExclusive;
  
  public ExcelRowSource(final Sheet sheet) {
    this(sheet, 0, sheet.getLastRowNum() + 1);
  }
  
  public ExcelRowSource(final Sheet sheet, int startInclusive, int endExclusive) {
    this.sheet = sheet;
    
    if ( startInclusive < 0 ) throw new IllegalArgumentException();
    if ( endExclusive >= this.sheet.getLastRowNum() ) throw new IllegalArgumentException();
    
    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;
  }
  
  @Override
  public final Class<GulpRow> coreType() {
    return GulpRow.class;
  }
  
  public final void forEach(final Processor<? super GulpRow> processor) {
    for ( int i = this.startInclusive; i < this.endExclusive; ++i ) {
      ExcelRow row = new ExcelRow(this.sheet.getRow(i));
      
      processor.process(row);
    }
  }
  
  public final ExcelRowSource sub(final int startIndexInclusive) {
    return new ExcelRowSource(this.sheet, this.startInclusive + startIndexInclusive, this.endExclusive);
  }
  
  public final ExcelRowSource sub(
    final int startIndexInclusive,
    final int endIndexExclusive)
  {
    int newStart = this.startInclusive + startIndexInclusive;
    int newEnd = newStart + endIndexExclusive;
    
    if ( newEnd > this.endExclusive ) throw new IllegalArgumentException();
    return new ExcelRowSource(this.sheet, newStart, newEnd);
  }
  
  @Override
  public final <V> Converter<GulpRow, V> converterFor(final Class<V> type) {
    return new GulpRowConverter<V>(type);
  }
}
