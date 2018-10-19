package com.azul.gulp.excel;

import org.apache.poi.ss.usermodel.Sheet;

import com.azul.gulp.sources.Normalizers;
import com.azul.gulp.sources.Source;
import com.azul.gulp.sources.SourceBasedGulpLog;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpSheet;

final class ExcelSheet
  extends SourceBasedGulpLog<GulpSheet, GulpRow>
  implements GulpSheet
{
  ExcelSheet(final Sheet sheet) {
    super(new ExcelRowSource(sheet));
  }
  
  private ExcelSheet(final Source<GulpRow> source, final Normalizers normalizers) {
    super(source, normalizers);
  }
    
  @Override
  protected final GulpSheet createOffspring(Normalizers normalizers) {
    return new ExcelSheet(this.source(), normalizers);
  }
  
  @Override
  protected final ExcelSheet createOffspring(final Source<GulpRow> source) {
    return new ExcelSheet(source, this.normalizers());
  }
  
  @Override
  public ExcelSheet subsheet(final int startIndexInclusive) {
    return this.createOffspring(this.<ExcelRowSource>source().sub(startIndexInclusive));
  }
  
  @Override
  public final ExcelSheet subsheet(final int startIndexInclusive, final int endIndexExclusive) {
    return this.createOffspring(this.<ExcelRowSource>source().sub(startIndexInclusive, endIndexExclusive));
  }
}
