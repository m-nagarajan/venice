package com.linkedin.venice.stats;

import io.tehuti.metrics.MetricConfig;


public class VeniceMetricsConfig {
  private final String serviceName;
  private final String metricPrefix;
  private final boolean emitDeltaTelemetry;
  private final boolean emitToFluentbit;
  private final boolean emitToPrometheus; // for debug purposes
  private final boolean emitToLog; // for debug purposes
  private final MetricConfig tehutiMetricConfig;

  private VeniceMetricsConfig(VeniceMetricsConfigBuilder veniceMetricsConfigBuilder) {
    this.serviceName = veniceMetricsConfigBuilder.serviceName;
    this.metricPrefix = veniceMetricsConfigBuilder.metricPrefix;
    this.emitDeltaTelemetry = veniceMetricsConfigBuilder.emitDeltaTelemetry;
    this.emitToFluentbit = veniceMetricsConfigBuilder.emitToFluentbit;
    this.emitToPrometheus = veniceMetricsConfigBuilder.emitToPrometheus;
    this.emitToLog = veniceMetricsConfigBuilder.emitToLog;
    this.tehutiMetricConfig = veniceMetricsConfigBuilder.tehutiMetricConfig;
  }

  public static class VeniceMetricsConfigBuilder {
    private String serviceName = "NOOP_SERVICE";
    private String metricPrefix = null;
    private boolean emitDeltaTelemetry = true;
    private boolean emitToFluentbit = false;
    private boolean emitToPrometheus = false;
    private boolean emitToLog = true;
    private MetricConfig tehutiMetricConfig = null;

    public VeniceMetricsConfigBuilder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public VeniceMetricsConfigBuilder setMetricPrefix(String metricPrefix) {
      this.metricPrefix = metricPrefix;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitDeltaTelemetry(boolean emitDeltaTelemetry) {
      this.emitDeltaTelemetry = emitDeltaTelemetry;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitToFluentbit(boolean emitToFluentbit) {
      this.emitToFluentbit = emitToFluentbit;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitToPrometheus(boolean emitToPrometheus) {
      this.emitToPrometheus = emitToPrometheus;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitToLog(boolean emitToLog) {
      this.emitToLog = emitToLog;
      return this;
    }

    public VeniceMetricsConfigBuilder setTehutiMetricConfig(MetricConfig tehutiMetricConfig) {
      this.tehutiMetricConfig = tehutiMetricConfig;
      return this;
    }

    // Validate required fields before building
    private void checkAndSetDefaults() {
      if (tehutiMetricConfig == null) {
        tehutiMetricConfig = new MetricConfig();
      }
      if (metricPrefix == null) {
        metricPrefix = serviceName;
      }
    }

    public VeniceMetricsConfig build() {
      checkAndSetDefaults();
      return new VeniceMetricsConfig(this);
    }
  }

  // all getters
  public String getServiceName() {
    return this.serviceName;
  }

  public String getMetricPrefix() {
    return this.metricPrefix;
  }

  public boolean isEmitDeltaTelemetry() {
    return emitDeltaTelemetry;
  }

  public boolean isEmitToFluentbit() {
    return emitToFluentbit;
  }

  public boolean isEmitToPrometheus() {
    return emitToPrometheus;
  }

  public boolean isEmitToLog() {
    return emitToLog;
  }

  public MetricConfig getTehutiMetricConfig() {
    return tehutiMetricConfig;
  }
}
