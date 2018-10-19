package com.azul.gulp.csv;

import com.azul.gulp.sources.PipelineConfiguration;
import com.azul.gulp.sources.Source;
import com.azul.gulp.sources.SourceBasedGulpLog;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpSheet;

public final class CsvSheet
  extends SourceBasedGulpLog<GulpSheet, GulpRow>
  implements GulpSheet
{
  CsvSheet(final CsvRowSource source) {
    super(source);
  }
  
  private CsvSheet(final Source<GulpRow> source, final PipelineConfiguration normalizers) {
    super(source, normalizers);
  }
    
  @Override
  protected final CsvSheet createOffspring(PipelineConfiguration normalizers) {
    return new CsvSheet(this.source(), normalizers);
  }
  
  @Override
  protected final CsvSheet createOffspring(final Source<GulpRow> source) {
    return new CsvSheet(source, this.normalizers());
  }
  
  public CsvSheet skipHeader() {
    return this.subsheet(1);
  }
  
  @Override
  public CsvSheet subsheet(final int startIndexInclusive) {
    return this.createOffspring(this.<CsvRowSource>source().sub(startIndexInclusive));
  }
  
  @Override
  public final CsvSheet subsheet(final int startIndexInclusive, final int endIndexExclusive) {
    return this.createOffspring(this.<CsvRowSource>source().sub(startIndexInclusive, endIndexExclusive));
  }
}
