package com.azul.gulp.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azul.gulp.Emitter;
import com.azul.gulp.LogProcessingException;
import com.azul.gulp.Processor;
import com.azul.gulp.io.IoProvider;
import com.azul.gulp.nexus.Nexus;
import com.azul.gulp.sources.Converter;
import com.azul.gulp.sources.Source;

public final class LineSource extends Source<Line> {
  private final Iterable<? extends IoProvider<Reader>> readerProviders;
  
  public LineSource(final File file) {
    this(() -> new FileReader(file));
  }
  
  public LineSource(final URL url) {
    this(() -> new InputStreamReader(url.openStream()));
  }
    
  public LineSource(final IoProvider<Reader> readerProvider) {
    this(Collections.singleton(readerProvider));
  }
  
  public LineSource(final File... files) {
    this(asProviders(files));
  }
  
  private static Iterable<? extends IoProvider<Reader>> asProviders(final File... files) {
    ArrayList<IoProvider<Reader>> readerProviders = new ArrayList<>(files.length);
    for ( File file: files ) {
      readerProviders.add(() -> new FileReader(file));
    }
    return readerProviders;
  }
  
  public LineSource(final Iterable<? extends IoProvider<Reader>> readerProviders) {
    this.readerProviders = readerProviders;
  }
  
  @Override
  public final Class<Line> coreType() {
    return Line.class;
  }
  
  @Override
  public <U> Converter<Line, U> converterFor(final Nexus nexus, final Class<U> type) {
    List<GulpText.LineMatcher> lineMatcherAnnos = nexus.findAnnotationsFor(type, GulpText.LineMatcher.class);
    if ( lineMatcherAnnos.isEmpty() ) return null;
    
    LineMatcher<U> lineMatcher = GulpText.makeMatcherFrom(type, lineMatcherAnnos);
    return new LineMatcherConverterAdapter<U>(type, lineMatcher);
  }
 
  @Override
  protected final void forEachImpl(final Processor<? super Line> processor) {
    for ( IoProvider<Reader> readerProvider: this.readerProviders ) {
      this.forEach(readerProvider, processor);
    }
  }
  
  protected final void forEach(
    IoProvider<Reader> readerProvider,
    Processor<? super Line> processor)
  {
    try ( BufferedReader reader = new BufferedReader(readerProvider.open()) ) {
      int curLineNumber = 0;
      for ( String line = reader.readLine(); line != null; line = reader.readLine() ) {
        try {
          processor.process(new Line(++curLineNumber, line, line));
        } catch ( Exception e ) {
          throw new LogProcessingException(e);
        }
      }
    } catch ( IOException e ) {
      throw new LogProcessingException(e);
    }
  }
  
  private static final class LineMatcherConverterAdapter<T> extends Converter<Line, T> {
    private final Class<T> type;
    private final LineMatcher<T> matcher;
    
    public LineMatcherConverterAdapter(
      final Class<T> type,
      final LineMatcher<T> matcher)
    {
      this.type = type;
      this.matcher = matcher;
    }
    
    @Override
    public final void init(final Nexus ctx) {
      ctx.inject(this.matcher);
    }
    
    @Override
    public final void convert(final Line input, Emitter<T> emitter)
      throws Exception
    {
      this.matcher.process(input, emitter);
    }
  }
}