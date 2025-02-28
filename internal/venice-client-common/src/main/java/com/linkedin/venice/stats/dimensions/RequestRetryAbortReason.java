package com.linkedin.venice.stats.dimensions;

public enum RequestRetryAbortReason implements VeniceDimensionInterface {
  SLOW_ROUTE, DELAY_CONSTRAINT, MAX_RETRY_ROUTE_LIMIT, NO_AVAILABLE_REPLICA;

  private final String abortReason;

  RequestRetryAbortReason() {
    this.abortReason = name().toLowerCase();
  }

  @Override
  public VeniceMetricsDimensions getDimensionName() {
    return VeniceMetricsDimensions.VENICE_REQUEST_RETRY_ABORT_REASON;
  }

  @Override
  public String getDimensionValue() {
    return this.abortReason;
  }
}
