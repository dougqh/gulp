package com.azul.gulp;

public interface GulpStreamExtension<E extends GulpStreamExtension<E, T>, T> 
  extends GulpStream<T>
{
}
