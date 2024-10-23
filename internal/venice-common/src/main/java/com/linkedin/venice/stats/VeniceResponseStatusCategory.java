package com.linkedin.venice.stats;

public enum VeniceResponseStatusCategory {
  HEALTHY("healthy"), UNHEALTHY("unhealthy"), TARDY("tardy"), THROTTLED("throttled");

  private final String category;

  VeniceResponseStatusCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return this.category;
  }
}
