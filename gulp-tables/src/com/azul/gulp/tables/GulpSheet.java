package com.azul.gulp.tables;

import com.azul.gulp.GulpStream;
import com.azul.gulp.Normalizer;
import com.azul.gulp.Predicate;
import com.azul.gulp.foundations.GulpLogExtension;

public interface GulpSheet extends GulpLogExtension<GulpSheet> {
  public abstract GulpSheet subsheet(final int startIndexInclusive);
  
  public abstract GulpSheet subsheet(
    final int startIndexInclusive,
    final int endIndexExclusive);
  
  public default GulpSheet skipHeader() {
    return this.subsheet(1);
  }
  
  public default GulpStream<GulpRow> rows() {
    return this.select(GulpRow.class);
  }
  
  public default GulpStream<GulpRow> rows(int startIndexInclusive) {
    return this.subsheet(startIndexInclusive).rows();
  }
  
  public default GulpStream<GulpRow> rows(
    final int startIndexInclusive,
    final int endIndexExclusive)
  {
    return this.subsheet(startIndexInclusive, endIndexExclusive).rows();
  }
  
  public default GulpSheet normalize(final Normalizer<GulpRow> rowNormalizer) {
    return this.normalize(GulpRow.class, rowNormalizer);
  }
  
  public default GulpSheet filter(final Predicate<? super GulpRow> predicateFn) {
    return this.normalize(row -> predicateFn.matches(row) ? row : null);
  }
  
  public default GulpSheet filterOut(final Predicate<? super GulpRow> predicateFn) {
    return this.normalize(row -> predicateFn.matches(row) ? null : row);
  }
}
