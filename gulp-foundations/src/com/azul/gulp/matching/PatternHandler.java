package com.azul.gulp.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azul.gulp.nexus.FlexNexusHandler;
import com.azul.gulp.nexus.Nexus;


public abstract class PatternHandler extends FlexNexusHandler {
  private final List<PatternFragment> fragments;
  private final Launcher launcher;
  
  private Nexus engine;
  
  public PatternHandler() {
    this.fragments = this.pattern().fragments();
    
    Launcher launcher;
    if ( fragments.isEmpty() ) {
      throw new IllegalStateException();
    } else if ( fragments.size() == 1 ) {
      if ( fragments.get(0).isMany() ) {
        launcher = new MatchManyTrivialLauncher();
      } else {
        launcher = new MatchOneTrivialLauncher();
      }
    } else {
      throw new UnsupportedOperationException();
      //launcher = new NonTrivialLauncher();
    }
    this.launcher = launcher;
  }
  
  protected abstract Pattern pattern();
  
  protected abstract void match(Match match) throws Exception;
  
  @Override
  public void init(Nexus engine) throws Exception {
    this.engine = engine;
    for ( PatternFragment fragment: this.fragments ) {
      engine.require(fragment.type());
    }
  }
  
  @Override
  public final Class<?> type() {
    return this.fragments.get(0).type();
  }
  
  @Override
  public final void handle(final Object value) throws Exception {
    this.launcher.handle(this.engine, value);
  }
  
  @Override
  public void finish() throws Exception {
    this.launcher.finish();
  }
  
  protected abstract class Launcher {
    abstract void handle(Nexus engine, Object value) throws Exception;
    
    abstract void finish() throws Exception;
  }
  
  protected class MatchOneTrivialLauncher extends Launcher {
    @Override
    void handle(final Nexus engine, final Object value) throws Exception {
      Match match = new Match() {
        @Override
        public <T> T get(final int index) {
          if ( index < 0 || index > 1 ) throw new IllegalArgumentException();
          
          @SuppressWarnings("unchecked")
          T castedValue = (T)value;
          return castedValue;
        }
        
        @Override
        public final <T> List<T> list(final int index) {
          throw new IllegalStateException();
        }
      };
      
      PatternHandler.this.match(match);
    }
    
    @Override
    void finish() {}
  }
  
  protected class MatchManyTrivialLauncher extends Launcher {
    private final List<Object> list = new ArrayList<>();
    
    @Override
    void handle(final Nexus engine, final Object value) {
      this.list.add(value);
    }
    
    @Override
    void finish() throws Exception {
      Match match = new Match() {
        @Override
        public <T> T get(final int index) {
          throw new IllegalStateException();
        }
        
        @Override
        public final <T> List<T> list(final int index) {
          return Collections.singletonList(this.<T>get(index));
        }
      };
      
      PatternHandler.this.match(match);
    }
  }
  
  protected final class NonTrivialLauncher extends Launcher {
    private final List<PatternFragment> fragments;
    
    NonTrivialLauncher(final List<PatternFragment> fragments) {
      this.fragments = fragments;
    }
    
    @Override
    void handle(final Nexus engine, final Object value) throws Exception {
      throw new UnsupportedOperationException("unfinished");
//      Object[] match = new Object[this.fragments.size()];
//      
//      FragmentHandler handler = new FragmentHandler(
//        match,
//        this.fragments.get(0),
//        this.fragments.subList(1, this.fragments.size()));
//      
//      handler.handle(value);
    }
    
    @Override
    void finish() throws Exception {
      
    }
  }
  
  private final class MatchBuilder {
    
  }
}
