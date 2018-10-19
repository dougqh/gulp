package com.azul.gulp.csv;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.azul.gulp.Processor;
import com.azul.gulp.io.IoProvider;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpRowSource;

public final class CsvRowSource extends GulpRowSource {
  private static final Charset CP_1252 = Charset.forName("CP1252");
  private static final Charset DEFAULT_CHARSET = CP_1252;
  
  private static final CSVFormat DEFAULT_FORMAT = CSVFormat.EXCEL;
  
  private final IoProvider<CSVParser> parserProvider;
  
  private final Integer startInclusive;
  private final Integer endExclusive;
  
  public CsvRowSource(final File file) {
    // encoding?
    this(() -> CSVParser.parse(file, DEFAULT_CHARSET, DEFAULT_FORMAT));
  }
  
  public CsvRowSource(final File file, final char delimiter) {
    this(() -> CSVParser.parse(
      file,
      DEFAULT_CHARSET,
      DEFAULT_FORMAT.withDelimiter(delimiter))
    );
  }
  
  public CsvRowSource(final IoProvider<CSVParser> parserProvider) {
    this.parserProvider = parserProvider;
    this.startInclusive = null;
    this.endExclusive = null;
  }
  
  CsvRowSource(
    final IoProvider<CSVParser> parserProvider,
    final int startInclusive)
  {
    this.parserProvider = parserProvider;
    
    if ( startInclusive < 0 ) throw new IllegalArgumentException();
        
    this.startInclusive = startInclusive;
    this.endExclusive = null;
  }
    
  CsvRowSource(
    final IoProvider<CSVParser> parserProvider,
    final int startInclusive, final int endExclusive)
  {
    this.parserProvider = parserProvider;
    
    if ( startInclusive < 0 ) throw new IllegalArgumentException();
    if ( endExclusive < startInclusive ) throw new IllegalArgumentException();
    
    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;
  }
    
  public final CsvRowSource sub(final int startIndexInclusive) {
    int newStart = (this.startInclusive == null ) ? startIndexInclusive : this.startInclusive + startIndexInclusive;
    
    if ( this.endExclusive == null ) {
      return new CsvRowSource(this.parserProvider, newStart);
    } else {
      return new CsvRowSource(this.parserProvider, newStart, this.endExclusive);
    }
  }
  
  public final CsvRowSource sub(
    final int startIndexInclusive,
    final int endIndexExclusive)
  {
    int newStart = (this.startInclusive == null ) ? startIndexInclusive : this.startInclusive + startIndexInclusive;
    int newEnd = newStart + endIndexExclusive;
    
    if ( this.endExclusive != null && newEnd > this.endExclusive ) throw new IllegalArgumentException();
    
    return new CsvRowSource(this.parserProvider, newStart, newEnd);
  }
  
  @Override
  protected final void forEachImpl(final Processor<? super GulpRow> processor) throws Exception {
    try ( CSVParser parser = this.parserProvider.open() ) {
      int rowIndex = 0;
      for ( CSVRecord record: parser ) {
        if ( inRange(rowIndex) ) {
          processor.process(new CsvRow(record));
        }
        rowIndex += 1;
      }
    }
  }
  
  private final boolean inRange(final int rowIndex) {
    if ( this.startInclusive != null ) {
      if ( rowIndex < this.startInclusive ) return false;
    }
    
    if ( this.endExclusive != null ) {
      if ( rowIndex >= this.endExclusive ) return false;
    }
    
    return true;
  }
}
