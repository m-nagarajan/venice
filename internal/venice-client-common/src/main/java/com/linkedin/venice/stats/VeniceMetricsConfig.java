package com.linkedin.venice.stats;

import io.tehuti.metrics.MetricConfig;


public class VeniceMetricsConfig {
  private final String serviceName;
  private final String metricPrefix;
  private final boolean emitDeltaTelemetry;
  private final boolean emitToHttpEndpoint;
  private final String httpEndpoint;
  private final boolean emitToPrometheus; // for debug purposes
  private final int prometheusPort; // for debug purposes
  private final boolean emitToLog; // for debug purposes
  private final boolean useExponentialHistogram;
  private final int exponentialHistogramMaxScale;
  private final int exponentialHistogramMaxBuckets;
  private final MetricConfig tehutiMetricConfig;

  private VeniceMetricsConfig(VeniceMetricsConfigBuilder veniceMetricsConfigBuilder) {
    this.serviceName = veniceMetricsConfigBuilder.serviceName;
    this.metricPrefix = veniceMetricsConfigBuilder.metricPrefix;
    this.emitDeltaTelemetry = veniceMetricsConfigBuilder.emitDeltaTelemetry;
    this.emitToHttpEndpoint = veniceMetricsConfigBuilder.emitToHttpEndpoint;
    this.httpEndpoint = veniceMetricsConfigBuilder.httpEndpoint;
    this.emitToPrometheus = veniceMetricsConfigBuilder.emitToPrometheus;
    this.prometheusPort = veniceMetricsConfigBuilder.prometheusPort;
    this.emitToLog = veniceMetricsConfigBuilder.emitToLog;
    this.useExponentialHistogram = veniceMetricsConfigBuilder.useExponentialHistogram;
    this.exponentialHistogramMaxScale = veniceMetricsConfigBuilder.exponentialHistogramMaxScale;
    this.exponentialHistogramMaxBuckets = veniceMetricsConfigBuilder.exponentialHistogramMaxBuckets;
    this.tehutiMetricConfig = veniceMetricsConfigBuilder.tehutiMetricConfig;
  }

  public static class VeniceMetricsConfigBuilder {
    private String serviceName = "NOOP_SERVICE";
    private String metricPrefix = null;
    private boolean emitDeltaTelemetry = true;
    private boolean emitToHttpEndpoint = false;
    private String httpEndpoint = null;
    private boolean emitToPrometheus = false;
    private int prometheusPort = 9464;
    private boolean emitToLog = true;
    private boolean useExponentialHistogram = true;
    private int exponentialHistogramMaxScale = 3;
    private int exponentialHistogramMaxBuckets = 250;
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

    public VeniceMetricsConfigBuilder setEmitToHttpEndpoint(boolean emitToHttpEndpoint) {
      this.emitToHttpEndpoint = emitToHttpEndpoint;
      return this;
    }

    public VeniceMetricsConfigBuilder setHttpEndpoint(String httpEndpoint) {
      this.httpEndpoint = httpEndpoint;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitToPrometheus(boolean emitToPrometheus) {
      this.emitToPrometheus = emitToPrometheus;
      return this;
    }

    public VeniceMetricsConfigBuilder setPrometheusPort(int prometheusPort) {
      this.prometheusPort = prometheusPort;
      return this;
    }

    public VeniceMetricsConfigBuilder setEmitToLog(boolean emitToLog) {
      this.emitToLog = emitToLog;
      return this;
    }

    public VeniceMetricsConfigBuilder setUseExponentialHistogram(boolean useExponentialHistogram) {
      this.useExponentialHistogram = useExponentialHistogram;
      return this;
    }

    public VeniceMetricsConfigBuilder setExponentialHistogramMaxScale(int exponentialHistogramMaxScale) {
      this.exponentialHistogramMaxScale = exponentialHistogramMaxScale;
      return this;
    }

    public VeniceMetricsConfigBuilder setExponentialHistogramMaxBuckets(int exponentialHistogramMaxBuckets) {
      this.exponentialHistogramMaxBuckets = exponentialHistogramMaxBuckets;
      return this;
    }

    public VeniceMetricsConfigBuilder setTehutiMetricConfig(MetricConfig tehutiMetricConfig) {
      this.tehutiMetricConfig = tehutiMetricConfig;
      return this;
    }

    /** get the last part of the service name
     * For instance: if service name is "venice-router", return "router"
     */
    public static String getMetricsPrefix(String input) {
      String[] parts = input.split("[\\-\\._]");
      String lastPart = parts[parts.length - 1];
      return lastPart;
    }

    // Validate required fields before building
    private void checkAndSetDefaults() {
      if (tehutiMetricConfig == null) {
        tehutiMetricConfig = new MetricConfig();
      }
      if (metricPrefix == null) {
        metricPrefix = getMetricsPrefix(serviceName);
      }
      // temp
      if (emitToHttpEndpoint) {
        httpEndpoint = "http://[::1]:22784/v1/metrics";
      }
      /*      if (emitToHttpEndpoint && httpEndpoint == null) {
        throw new IllegalArgumentException("httpEndpoint must be set if emitToHttpEndpoint is true");
      }*/
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

  public boolean isEmitToHttpEndpoint() {
    return emitToHttpEndpoint;
  }

  public String getHttpEndpoint() {
    return httpEndpoint;
  }

  public boolean isEmitToPrometheus() {
    return emitToPrometheus;
  }

  public int getPrometheusPort() {
    return prometheusPort;
  }

  public boolean isEmitToLog() {
    return emitToLog;
  }

  public boolean isUseExponentialHistogram() {
    return useExponentialHistogram;
  }

  public int getExponentialHistogramMaxScale() {
    return exponentialHistogramMaxScale;
  }

  public int getExponentialHistogramMaxBuckets() {
    return exponentialHistogramMaxBuckets;
  }

  public MetricConfig getTehutiMetricConfig() {
    return tehutiMetricConfig;
  }

  @Override
  public String toString() {
    return "VeniceMetricsConfig{" + "serviceName='" + serviceName + '\'' + ", metricPrefix='" + metricPrefix + '\''
        + ", emitDeltaTelemetry=" + emitDeltaTelemetry + ", emitToHttpEndpoint=" + emitToHttpEndpoint
        + ", httpEndpoint='" + httpEndpoint + '\'' + ", emitToPrometheus=" + emitToPrometheus + ", prometheusPort="
        + prometheusPort + ", emitToLog=" + emitToLog + ", useExponentialHistogram=" + useExponentialHistogram
        + ", tehutiMetricConfig=" + tehutiMetricConfig + '}';
  }
}
