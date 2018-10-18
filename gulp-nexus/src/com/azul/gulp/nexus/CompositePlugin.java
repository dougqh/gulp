package com.azul.gulp.nexus;

import java.util.List;

public class CompositePlugin extends Plugin {
  private final List<Plugin> plugins;
  
  public CompositePlugin(final List<Plugin> plugins) {
    this.plugins = plugins;
  }
  
  @Override
  public <V> boolean handleEventRequest(
    final Nexus engine,
    final Class<V> requiredType)
    throws Exception
  {
    boolean handled = false;
    for ( Plugin plugin: this.plugins ) {
      handled |= plugin.handleEventRequest(engine, requiredType);
    }
    return handled;
  }
  
  @Override
  public <V> void onEventRequest(
    final Nexus engine,
    final Class<V> requiredType) throws Exception
  {
    for ( Plugin plugin: this.plugins ) {
      plugin.onEventRequest(engine, requiredType);
    }
  }
  
  public <V> boolean connect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    boolean connected = false;
    for ( Plugin plugin: this.plugins ) {
      connected |= plugin.connect(engine, object);
    }
    return connected;
  }
  
  public <V> void onConnect(
    final Nexus engine,
    final Object object)
    throws Exception
  {
    for ( Plugin plugin: this.plugins ) {
      plugin.onConnect(engine, object);
    }
  }
}
