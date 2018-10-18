package com.azul.gulp.nexus;

public abstract class Plugin {
  public <V> boolean handleEventRequest(
    final Nexus engine,
    final Class<V> requiredType)
    throws Exception
  {
    return false;
  }
  
  public <V> void onEventRequest(
    final Nexus engine,
    final Class<V> requiredType)
    throws Exception
  { 
  }
  
  public <V> boolean connect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    return false;
  }
  
  public <V> void onConnect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
  }
}
