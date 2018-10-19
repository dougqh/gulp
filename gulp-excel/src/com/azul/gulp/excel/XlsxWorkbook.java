package com.azul.gulp.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;

import com.azul.gulp.io.IoProvider;
import com.azul.gulp.tables.GulpSheet;
import com.azul.gulp.tables.GulpWorkbook;

final class XlsxWorkbook implements GulpWorkbook {
  private final IoProvider<XlsxFile> xlsxProvider;
  
  XlsxWorkbook(final IoProvider<InputStream> streamProvider) {
    this.xlsxProvider = new IoProvider<XlsxFile>() {
      @Override
      public final XlsxFile open() throws IOException {
        return XlsxFile.open(streamProvider);
      }
    };
  }
    
  @Override
  public final XslxSheet curSheet() {
    // TODO: proper implementation
    return this.sheetAt(0);
  }
  
  @Override
  public final XslxSheet sheetAt(final int index) {
    return new XslxSheet(this.xlsxProvider, (curIndex, curName) -> curIndex == index);
  }
  
  @Override
  public final GulpSheet sheetByName(String name) {
    return new XslxSheet(this.xlsxProvider, (curIndex, curName) -> curName.equals(name));
  }
  
  @Override
  public final Iterable<GulpSheet> sheets() {
    return new Iterable<GulpSheet>() {
      @Override
      public final Iterator<GulpSheet> iterator() {
        return XlsxWorkbook.this.sheetIterator();
      }
    };
  }
  
  private final Iterator<GulpSheet> sheetIterator() {
    List<String> names = new ArrayList<>();
    
    // TODO: This is quite wasteful, but so be it.
    try ( XlsxFile file = this.xlsxProvider.open() ) {
      for ( SheetIterator iter = file.sheetIterator(); iter.hasNext(); ) {
        names.add(iter.getSheetName());
      }
    } catch (IOException e) {
      throw new RuntimeException(e); 
    }
    
    return names.stream().map(this::sheetByName).iterator();
  }
}
