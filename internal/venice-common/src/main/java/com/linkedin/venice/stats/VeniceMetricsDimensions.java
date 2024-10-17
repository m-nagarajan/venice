package com.linkedin.venice.stats;

public enum VeniceMetricsDimensions {
  VENICE_STORE_NAME("Venice.Store.Name"),

  /** {@link com.linkedin.venice.read.RequestType} */
  VENICE_REQUEST_METHOD("Venice.Request.Method"),

  /** {@link io.netty.handler.codec.http.HttpResponseStatus} */
  HTTP_RESPONSE_STATUS_CODE("Http.Response.StatusCode"),

  /** {@link io.netty.handler.codec.http.HttpStatusClass} */
  HTTP_RESPONSE_STATUS_CODE_CATEGORY("Http.Response.StatusCodeCategory"),

  /** {@link VeniceResponseStatus} */
  VENICE_RESPONSE_STATUS_CODE("Venice.Response.StatusCode");

  private final String dimensionName;

  VeniceMetricsDimensions(String dimensionName) {
    this.dimensionName = dimensionName;
  }

  public String getDimensionName() {
    return this.dimensionName;
  }
}
