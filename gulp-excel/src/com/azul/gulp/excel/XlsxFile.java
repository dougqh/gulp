package com.azul.gulp.excel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.SAXException;

import com.azul.gulp.io.IoProvider;

public class XlsxFile implements AutoCloseable {
  public static final XlsxFile open(final IoProvider<InputStream> streamProvider)
    throws IOException
  {
    try {
      return new XlsxFile(OPCPackage.open(streamProvider.open()));
    } catch ( InvalidFormatException e ) {
      throw new IOException(e);
    }
  }
  
  private final OPCPackage pkg;
  private final XSSFReader reader;
  
  final ReadOnlySharedStringsTable stringTable;
  final StylesTable styleTable;
  
  
  private XlsxFile(final OPCPackage pkg) throws IOException {
    this.pkg = pkg;
    try {
      this.stringTable = new ReadOnlySharedStringsTable(pkg);
    
      this.reader = new XSSFReader(pkg);
      this.styleTable = reader.getStylesTable();
    } catch ( SAXException | OpenXML4JException e ) {
      throw new IOException(e);
    }
  }
  
  public final SheetIterator sheetIterator() throws IOException {
    try {
      return (XSSFReader.SheetIterator)this.reader.getSheetsData();
    } catch ( InvalidFormatException e ) {
      throw new IOException(e);
    }
  }
  
  @Override
  public void close() throws IOException {
    this.pkg.close();
  }
}
