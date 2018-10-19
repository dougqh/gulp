package com.azul.gulp.excel;

import com.azul.gulp.io.IoProvider;
import com.azul.gulp.sources.PipelineConfiguration;
import com.azul.gulp.sources.Source;
import com.azul.gulp.sources.SourceBasedGulpLog;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpSheet;

final class XslxSheet
  extends SourceBasedGulpLog<GulpSheet, GulpRow>
  implements GulpSheet
{
  XslxSheet(final IoProvider<XlsxFile> xlsxProvider, final ExcelSheetSelector selector) {
    super(new XlsxRowSource(xlsxProvider, selector));
  }
  
  private XslxSheet(final Source<GulpRow> source, final PipelineConfiguration normalizers) {
    super(source, normalizers);
  }
    
  @Override
  protected final XslxSheet createOffspring(PipelineConfiguration normalizers) {
    return new XslxSheet(this.source(), normalizers);
  }
  
  @Override
  protected final XslxSheet createOffspring(final Source<GulpRow> source) {
    return new XslxSheet(source, this.normalizers());
  }
  
  @Override
  public XslxSheet subsheet(final int startIndexInclusive) {
    return this.createOffspring(this.<XlsxRowSource>source().sub(startIndexInclusive));
  }
  
  @Override
  public final XslxSheet subsheet(final int startIndexInclusive, final int endIndexExclusive) {
    return this.createOffspring(this.<XlsxRowSource>source().sub(startIndexInclusive, endIndexExclusive));
  }
}
