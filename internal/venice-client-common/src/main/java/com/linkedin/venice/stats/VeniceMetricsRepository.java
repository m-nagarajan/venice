package com.linkedin.venice.stats;

import io.tehuti.metrics.MetricsRepository;
import java.io.Closeable;


/** extends MetricsRepository to keep the changes to a minimum. Next step would be to create a MetricsRepository inside rather than extending it */
public class VeniceMetricsRepository extends MetricsRepository implements Closeable {
  VeniceOpenTelemetryMetricsRepository openTelemetryMetricsRepository;

  public VeniceMetricsRepository() {
    super();
    openTelemetryMetricsRepository =
        new VeniceOpenTelemetryMetricsRepository(new VeniceMetricsConfig.VeniceMetricsConfigBuilder().build());
  }

  public VeniceMetricsRepository(VeniceMetricsConfig metricsConfig) {
    super(metricsConfig.getTehutiMetricConfig());
    openTelemetryMetricsRepository = new VeniceOpenTelemetryMetricsRepository(metricsConfig);
  }

  public VeniceOpenTelemetryMetricsRepository getOpenTelemetryMetricsRepository() {
    return openTelemetryMetricsRepository;
  }

  @Override
  public void close() {
    super.close();
    openTelemetryMetricsRepository.close();
  }
}
