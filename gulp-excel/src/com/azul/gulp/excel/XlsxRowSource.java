package com.azul.gulp.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.azul.gulp.Processor;
import com.azul.gulp.StreamProcessingException;
import com.azul.gulp.io.IoProvider;
import com.azul.gulp.tables.GulpRow;
import com.azul.gulp.tables.GulpRowSource;


final class XlsxRowSource extends GulpRowSource {
  private final IoProvider<XlsxFile> xlsxProvider;
  private final ExcelSheetSelector selector;
  
  private final Integer startInclusive;
  private final Integer endExclusive;
  
  XlsxRowSource(final IoProvider<XlsxFile> xlsxProvider, final ExcelSheetSelector selector) {
    this.xlsxProvider = xlsxProvider;
    this.selector = selector;
    this.startInclusive = null;
    this.endExclusive = null;
  }
  
  XlsxRowSource(
    final IoProvider<XlsxFile> xlsxProvider,
    final ExcelSheetSelector selector,
    final int startInclusive)
  {
    this.xlsxProvider = xlsxProvider;
    this.selector = selector;
    
    if ( startInclusive < 0 ) throw new IllegalArgumentException();
        
    this.startInclusive = startInclusive;
    this.endExclusive = null;
  }
  
  XlsxRowSource(
    final IoProvider<XlsxFile> xlsxProvider,
    final ExcelSheetSelector selector,
    final int startInclusive, final int endExclusive)
  {
    this.xlsxProvider = xlsxProvider;
    this.selector = selector;
    
    if ( startInclusive < 0 ) throw new IllegalArgumentException();
    if ( endExclusive < startInclusive ) throw new IllegalArgumentException();
    
    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;
  }
  
  public final XlsxRowSource sub(final int startIndexInclusive) {
    int newStart = (this.startInclusive == null ) ? startIndexInclusive : this.startInclusive + startIndexInclusive;
    
    if ( this.endExclusive == null ) {
      return new XlsxRowSource(this.xlsxProvider, this.selector, newStart);
    } else {
      return new XlsxRowSource(this.xlsxProvider, this.selector, newStart, this.endExclusive);
    }
  }
  
  public final XlsxRowSource sub(
    final int startIndexInclusive,
    final int endIndexExclusive)
  {
    int newStart = (this.startInclusive == null ) ? startIndexInclusive : this.startInclusive + startIndexInclusive;
    int newEnd = newStart + endIndexExclusive;
    
    if ( this.endExclusive != null && newEnd > this.endExclusive ) throw new IllegalArgumentException();
    
    return new XlsxRowSource(this.xlsxProvider, this.selector, newStart, newEnd);
  }
  
  protected final void forEachImpl(final Processor<? super GulpRow> processor) {
    try {
      try ( XlsxFile xlsxFile = this.xlsxProvider.open() ) {
        SheetIterator iter = xlsxFile.sheetIterator();
        int index = 0;
        while ( iter.hasNext() ) {
          try ( InputStream sheetStream = iter.next() ) {
            if ( this.selector.matches(index, iter.getSheetName()) ) {
              this.processSheet(xlsxFile, sheetStream, processor);
            }
          }
        }
      }
    } catch ( IOException | SAXException e ) {
      throw new RuntimeException(e);
    }
  }
  
  private final void processSheet(
    final XlsxFile xlsxFile,
    final InputStream in,
    final Processor<? super GulpRow> processor)
    throws IOException, SAXException
  {
    XMLReader xmlReader;
    try {
      xmlReader = SAXHelper.newXMLReader();
    } catch ( SAXException | ParserConfigurationException e ) {
      throw new IllegalStateException(e);
    }    
    
    SheetHandlerImpl sheetHandler = new SheetHandlerImpl(
      this.startInclusive,
      this.endExclusive,
      processor);
    
    ContentHandler contentHandler = new XSSFSheetXMLHandler(
      xlsxFile.styleTable,
      xlsxFile.stringTable,
      sheetHandler,
      sheetHandler.formatter(),
      false);
    
    xmlReader.setContentHandler(contentHandler);
    
    xmlReader.parse(new InputSource(in));
  }
  
  private class SheetHandlerImpl 
    extends DataFormatter
    implements SheetContentsHandler
  {
    private final Integer startInclusive;
    private final Integer endExclusive;
    private final Processor<? super GulpRow> processor;

    private Double doubleValue = null;
    
    private List<Object> values = null;
    
    SheetHandlerImpl(
      final Integer startIndexInclusive, final Integer endIndexExclusive,
      final Processor<? super GulpRow> processor)
    {
      this.startInclusive = startIndexInclusive;
      this.endExclusive = endIndexExclusive;
      this.processor = processor;
    }
    
    public final DataFormatter formatter() {
      return new DataFormatter() {
        @Override
        public String formatRawCellContents(double arg0, int arg1, String arg2, boolean arg3) {
          SheetHandlerImpl.this.doubleValue = arg0;
          
          return super.formatRawCellContents(arg0, arg1, arg2, arg3);
        }
        
        @Override
        public final String formatRawCellContents(double value, int formatIndex, String formatString) {
          SheetHandlerImpl.this.doubleValue = value;
          
          return super.formatRawCellContents(value, formatIndex, formatString);
        }
      };
    }
    
    @Override
    public void headerFooter(String arg0, boolean arg1, String arg2) {
      // ignore
    }
    
    @Override
    public final void startRow(final int rowIndex) {
      if ( this.inRange(rowIndex) ) {
        this.values = new ArrayList<>();
      }
    }
    
    @Override
    public final void cell(
      final String cellRefStr,
      final String formattedValue,
      final XSSFComment comment)
    {
      if ( this.values != null ) {
        CellReference cellRefObj = new CellReference(cellRefStr);
        int col = cellRefObj.getCol();
        
        while ( this.values.size() <= col ) {
          this.values.add(null);
        }
        if ( this.doubleValue != null ) {
          this.values.set(col, this.doubleValue);
        } else {
          this.values.set(col, formattedValue);
        }
      }
      
      this.doubleValue = null;
    }
    
    @Override
    public final void endRow(final int rowIndex) {
      if ( this.values != null ) {
        List<Object> curValues = this.values;
        this.values = null;
        
        try {
          this.processor.process(new XssfRow(curValues));
        } catch ( Exception e ) {
          throw new StreamProcessingException(e);
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
}
