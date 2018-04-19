package com.openkappa.bitrules;

import java.util.HashMap;
import java.util.Map;

public class Context {

  private final Map<String, Object> state = new HashMap<>();

  public Context(Map<String, Object> params) {
    this.state.putAll(params);
  }

  public <T> T get(String value) {
    return (T) state.get(value);
  }
}
