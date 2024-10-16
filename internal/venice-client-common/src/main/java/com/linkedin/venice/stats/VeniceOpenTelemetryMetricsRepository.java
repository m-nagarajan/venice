package com.linkedin.venice.stats;

import com.linkedin.venice.utils.concurrent.VeniceConcurrentHashMap;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VeniceOpenTelemetryMetricsRepository {
  private static final Logger LOGGER = LogManager.getLogger(VeniceOpenTelemetryMetricsRepository.class);

  private static final String FLUENTBIT_TAG_PATTERN =
      "serviceName:%s,instanceId:%s,containerName:%s,mdmAccount:%s,mdmNamespace:%s";
  private static final String FLUENTBIT_ENDPOINT = "http://[::1]:22784/v1/metrics";
  private static final String FLUENTBIT_HEADER_NAME = "X-LI-Fluentbit-Tag";

  private static final String NOOP_ACCOUNT = "noopAccount";
  private static final String NOOP_NAMESPACE = "noopNamespace";

  private OpenTelemetry openTelemetry = null;
  private SdkMeterProvider sdkMeterProvider = null;
  Meter meter;

  String metricPrefix;

  /** Below Maps are to create only one metric per name and type: Venice code will try to initialize the same metric multiple times as it will get
   * called from per store path and per request type path. This will ensure that we only have one metric per name and
   * use dimensions to differentiate between them.
   */
  private final VeniceConcurrentHashMap<String, DoubleHistogram> histogramMap = new VeniceConcurrentHashMap<>();
  private final VeniceConcurrentHashMap<String, LongCounter> counterMap = new VeniceConcurrentHashMap<>();

  public VeniceOpenTelemetryMetricsRepository(VeniceMetricsConfig metricsConfig) {
    LOGGER.info("OPENTELEMETRY INITIALIZATION STARTED");
    this.metricPrefix = "Venice." + metricsConfig.getMetricPrefix();
    final String fluentBitTag = String.format(
        FLUENTBIT_TAG_PATTERN,
        metricsConfig.getServiceName(),
        "i001", // instance
        metricsConfig.getServiceName(), // container name
        NOOP_ACCOUNT,
        NOOP_NAMESPACE);

    /* OtlpHttpMetricExporterBuilder exporterBuilder = OtlpHttpMetricExporter.builder().
            setEndpoint(FLUENTBIT_ENDPOINT).
            addHeader(FLUENTBIT_HEADER_NAME, fluentBitTag).
            setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred());
    
    SdkMeterProvider sdkMeterProvider =
            SdkMeterProvider.builder()
                    //.registerMetricReader(PeriodicMetricReader.builder(exporterBuilder.build()).build())
                    // Enable this local exporter to log metrics to console for troubleshooting
                    //.registerMetricReader(PeriodicMetricReader.builder(new LogBasedMetricExporter()).build())
                    .setResource(Resource.empty()) // Resource has to be empty so that Fluentbit can add them
                    .build();
    
    return OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProvider).build(); */

    try {
      SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
      if (metricsConfig.isEmitToPrometheus()) {
        int prometheusPort = 9464;
        builder.registerMetricReader(PrometheusHttpServer.builder().setPort(prometheusPort).build());
      }
      if (metricsConfig.isEmitToLog()) {
        builder.registerMetricReader(PeriodicMetricReader.builder(new LogBasedMetricExporter()).build());
      }
      sdkMeterProvider = builder.build();

      // Register MeterProvider with OpenTelemetry instance
      openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProvider).build();

      this.meter = openTelemetry.getMeter(metricPrefix);
      LOGGER.info("OPENTELEMETRY INITIALIZATION COMPLETED");
    } catch (Exception e) {
      LOGGER.error("OPENTELEMETRY INITIALIZATION FAILED", e);
    }
  }

  public DoubleHistogram getHistogram(String name, String unit, String description) {
    String fullMetricName = metricPrefix + "." + name;
    if (openTelemetry != null) {
      return histogramMap.computeIfAbsent(name, key -> {
        DoubleHistogramBuilder builder =
            meter.histogramBuilder(fullMetricName).setUnit(unit).setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("OpenTelemetry is not initialized");
      return null;
      // throw new Exception("OpenTelemetry is not initialized");
    }
  }

  public DoubleHistogram getHistogramWithoutBuckets(String name, String unit, String description) {
    String fullMetricName = metricPrefix + "." + name;
    if (openTelemetry != null) {
      return histogramMap.computeIfAbsent(name, key -> {
        DoubleHistogramBuilder builder = meter.histogramBuilder(fullMetricName)
            .setExplicitBucketBoundariesAdvice(new ArrayList<>())
            .setUnit(unit)
            .setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("OpenTelemetry is not initialized");
      return null;
      // throw new Exception("OpenTelemetry is not initialized");
    }
  }

  public LongCounter getCounter(String name, String unit, String description) {
    String fullMetricName = metricPrefix + "." + name;
    if (openTelemetry != null) {
      return counterMap.computeIfAbsent(name, key -> {
        LongCounterBuilder builder = meter.counterBuilder(fullMetricName).setUnit(unit).setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("OpenTelemetry is not initialized");
      return null;
      // throw new Exception("OpenTelemetry is not initialized");
    }
  }

  public void close() {
    LOGGER.info("OPENTELEMETRY CLOSE");
    sdkMeterProvider.shutdown();
    sdkMeterProvider = null;
  }

  static class LogBasedMetricExporter implements MetricExporter {
    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
      return AggregationTemporality.DELTA;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
      System.out.println("Exporting Otel metrics: " + metrics);
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
