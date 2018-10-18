package com.azul.gulp;

import java.io.PrintStream;

public interface Printable {
  public default String toPrintString() {
    return this.toString();
  }
  
  public default void print() {
    this.printTo(System.out);
  }
  
  public default void printTo(final PrintStream printStream) {
    printStream.println(this.toPrintString());
  }
}
