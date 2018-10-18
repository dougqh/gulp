package com.azul.gulp.nexus;

public interface NexusConfigurable {
  public default void init(final Nexus engine) throws Exception {}
}
