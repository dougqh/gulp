package com.azul.gulp.text;

import com.azul.gulp.sources.PipelineConfiguration;
import com.azul.gulp.sources.Source;
import com.azul.gulp.sources.SourceBasedGulpLog;

public final class GulpTextLog extends SourceBasedGulpLog<GulpTextLog, Line> {
  public GulpTextLog(final Source<Line> source) {
    super(source);
  }
  
  private GulpTextLog(final Source<Line> source, final PipelineConfiguration normalizers) {
    super(source, normalizers);
  }
  
  @Override
  protected final GulpTextLog createOffspring(final PipelineConfiguration normalizers) {
    return new GulpTextLog(this.source(), normalizers);
  }

  @Override
  protected GulpTextLog createOffspring(Source<Line> source) {
    return new GulpTextLog(source, this.normalizers());
  }
  
  public final <T> GulpTextLog normalize(final LineNormalizer normalizer) {
    return this.normalize(Line.class, LineNormalizers.toGenericNormalizer(normalizer));
  }
}
