package com.linkedin.venice.stats;

public enum VeniceMetricsDimensions {
  VENICE_STORE_NAME("venice.store.name"), VENICE_CLUSTER_NAME("venice.cluster.name"),

  /** {@link com.linkedin.venice.read.RequestType#requestTypeName} */
  VENICE_REQUEST_METHOD("venice.request.method"),

  /** {@link io.netty.handler.codec.http.HttpResponseStatus} ie. 200, 400, etc */
  HTTP_RESPONSE_STATUS_CODE("http.response.status_code"),

  /** {@link VeniceHttpResponseStatusCodeCategory#category} ie. 1xx, 2xx, etc */
  HTTP_RESPONSE_STATUS_CODE_CATEGORY("http.response.status_code_category"),

  /** {@link VeniceResponseStatusCategory} */
  VENICE_RESPONSE_STATUS_CODE_CATEGORY("venice.response.status_code_category");

  private final String dimensionName;

  VeniceMetricsDimensions(String dimensionName) {
    this.dimensionName = VeniceOpenTelemetryMetricsRepository.getDimensionName(dimensionName);
  }

  public String getDimensionName() {
    return this.dimensionName;
  }
}
