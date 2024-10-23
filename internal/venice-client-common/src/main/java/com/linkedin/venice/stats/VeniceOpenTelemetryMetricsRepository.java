package com.linkedin.venice.stats;

import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.utils.concurrent.VeniceConcurrentHashMap;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VeniceOpenTelemetryMetricsRepository {
  private static final Logger LOGGER = LogManager.getLogger(VeniceOpenTelemetryMetricsRepository.class);
  private static final String HTTP_EXPORTER_ENDPOINT = "http://[::1]:22784/v1/metrics";
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

  /*
  // OSS code
  OtlpHttpMetricExporter getOtlpHttpMetricExporter(VeniceMetricsConfig metricsConfig) {
    OtlpHttpMetricExporterBuilder httpExporterBuilder = OtlpHttpMetricExporter.builder()
        .setEndpoint(metricsConfig.getHttpEndpoint())
        .setAggregationTemporalitySelector(
            metricsConfig.isEmitDeltaTelemetry()
                ? AggregationTemporalitySelector.deltaPreferred()
                : AggregationTemporalitySelector.alwaysCumulative());
    return httpExporterBuilder.build();
  }*/

  // temp override in venice-backend
  OtlpHttpMetricExporter getOtlpHttpMetricExporter(VeniceMetricsConfig metricsConfig) {
    String FLUENTBIT_TAG_PATTERN = "serviceName:%s,instanceId:%s,containerName:%s,mdmAccount:%s,mdmNamespace:%s";
    String FLUENTBIT_HEADER_NAME = "X-LI-Fluentbit-Tag";
    String NOOP_ACCOUNT = "noopAccount";
    String NOOP_NAMESPACE = "noopNamespace";

    final String fluentBitTag = String.format(
        FLUENTBIT_TAG_PATTERN,
        "venice-router",
        "i350", // instance
        "venice-router", // container name
        NOOP_ACCOUNT,
        NOOP_NAMESPACE);

    LOGGER.info("OpenTelemetry fluentBitTag: {}", fluentBitTag);

    OtlpHttpMetricExporterBuilder httpExporterBuilder = OtlpHttpMetricExporter.builder()
        .setEndpoint(metricsConfig.getHttpEndpoint())
        .addHeader(FLUENTBIT_HEADER_NAME, fluentBitTag)
        .setAggregationTemporalitySelector(
            metricsConfig.isEmitDeltaTelemetry()
                ? AggregationTemporalitySelector.deltaPreferred()
                : AggregationTemporalitySelector.alwaysCumulative());
    return httpExporterBuilder.build();
  }

  public VeniceOpenTelemetryMetricsRepository(VeniceMetricsConfig metricsConfig) {
    LOGGER.info(
        "OpenTelemetry initialization for {} started with config: {}",
        metricsConfig.getServiceName(),
        metricsConfig.toString());
    this.metricPrefix = "venice." + metricsConfig.getMetricPrefix();

    try {
      SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
      if (metricsConfig.isEmitToHttpEndpoint()) {
        OtlpHttpMetricExporter httpExporter = getOtlpHttpMetricExporter(metricsConfig);
        builder.registerMetricReader(PeriodicMetricReader.builder(httpExporter).build());
      }
      if (metricsConfig.isEmitToPrometheus()) {
        // builder.registerMetricReader(PrometheusHttpServer.builder().setPort(metricsConfig.getPrometheusPort()).build());
      }
      if (metricsConfig.isEmitToLog()) {
        // internal to test: Disabled by default
        builder.registerMetricReader(PeriodicMetricReader.builder(new LogBasedMetricExporter()).build());
      }
      if (metricsConfig.isUseExponentialHistogram()) {
        builder.registerView(
            InstrumentSelector.builder().setName("*").setType(InstrumentType.HISTOGRAM).build(),
            View.builder()
                .setAggregation(
                    Aggregation.base2ExponentialBucketHistogram(
                        metricsConfig.getExponentialHistogramMaxBuckets(),
                        metricsConfig.getExponentialHistogramMaxScale()))
                .build());
      }
      builder.setResource(Resource.empty());
      sdkMeterProvider = builder.build();

      // Register MeterProvider with OpenTelemetry instance
      openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProvider).build();

      this.meter = openTelemetry.getMeter(getMetricPrefix(metricPrefix));
      LOGGER.info(
          "OpenTelemetry initialization for {} completed with config: {}",
          metricsConfig.getServiceName(),
          metricsConfig.toString());
    } catch (Exception e) {
      LOGGER.info(
          "OpenTelemetry initialization for {} failed with config: {}",
          metricsConfig.getServiceName(),
          metricsConfig.toString(),
          e);
      throw new VeniceException("OpenTelemetry initialization for " + metricsConfig.getServiceName() + " failed", e);
    }
  }

  private void validateMetricName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Metric name cannot be null or empty. Input name: " + name);
    }
    if (name.contains(" ")) {
      throw new IllegalArgumentException("Metric name cannot contain spaces. Input name: " + name);
    }
    // name should not contain any capital or special characters except for underscore and dot
    if (!name.matches("^[a-z0-9_.]*$")) {
      throw new IllegalArgumentException(
          "Metric name can only contain lowercase alphabets, numbers, underscore and dot. Input name: " + name);
    }
  }

  /*
   // OSS code
   public String getFullMetricName(String name) {
    String fullMetricName = metricPrefix + "." + name;
    validateMetricName(fullMetricName);
    return fullMetricName;
  }
  
  public String getDimensionName(String name) {
    validateMetricName(name);
    return name;
  }
  
  public String getMetricPrefix(String metricPrefix) {
    return metricPrefix;
  }
  */

  // temp override in venice-backend
  public String getFullMetricName(String name) {
    String fullMetricName = metricPrefix + "." + name;
    validateMetricName(fullMetricName);
    return convertOpenTelemetryNamingConventionToLI(fullMetricName);
  }

  public static String getDimensionName(String name) {
    return convertOpenTelemetryNamingConventionToLI(name);
  }

  public String getMetricPrefix(String metricPrefix) {
    return convertOpenTelemetryNamingConventionToLI(metricPrefix);
  }

  private static String convertOpenTelemetryNamingConventionToLI(String input) {
    String[] words = input.split("\\.");
    for (int i = 0; i < words.length; i++) {
      if (!words[i].isEmpty()) {
        String[] partWords = words[i].split("_");
        // make into Pascal case
        for (int j = 0; j < partWords.length; j++) {
          if (!partWords[j].isEmpty()) {
            partWords[j] = capitalizeFirstLetter(partWords[j]);
          }
        }
        StringBuilder sb = new StringBuilder();
        for (String partWord: partWords) {
          sb.append(partWord);
        }
        words[i] = sb.toString();
      }
    }
    StringBuilder finalName = new StringBuilder();
    for (String word: words) {
      finalName.append(word);
      finalName.append(".");
    }
    // remove the last dot
    if (finalName.length() > 0) {
      finalName.deleteCharAt(finalName.length() - 1);
    }
    return finalName.toString();
  }

  private static String capitalizeFirstLetter(String word) {
    if (word.isEmpty()) {
      return word;
    }
    return Character.toUpperCase(word.charAt(0)) + word.substring(1);
  }

  public DoubleHistogram getHistogram(String name, String unit, String description) {
    String fullMetricName = getFullMetricName(name);
    if (openTelemetry != null) {
      return histogramMap.computeIfAbsent(name, key -> {
        DoubleHistogramBuilder builder =
            meter.histogramBuilder(fullMetricName).setUnit(unit).setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("Metric instrument creation failed for metric {} because OpenTelemetry is not initialized", name);
      return null;
    }
  }

  public DoubleHistogram getHistogramWithoutBuckets(String name, String unit, String description) {
    String fullMetricName = getFullMetricName(name);
    if (openTelemetry != null) {
      return histogramMap.computeIfAbsent(name, key -> {
        DoubleHistogramBuilder builder = meter.histogramBuilder(fullMetricName)
            .setExplicitBucketBoundariesAdvice(new ArrayList<>())
            .setUnit(unit)
            .setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("Metric instrument creation failed for metric {} because OpenTelemetry is not initialized", name);
      return null;
    }
  }

  public LongCounter getCounter(String name, String unit, String description) {
    String fullMetricName = getFullMetricName(name);
    if (openTelemetry != null) {
      return counterMap.computeIfAbsent(name, key -> {
        LongCounterBuilder builder = meter.counterBuilder(fullMetricName).setUnit(unit).setDescription(description);
        return builder.build();
      });
    } else {
      LOGGER.error("Metric instrument creation failed for metric {} because OpenTelemetry is not initialized", name);
      return null;
    }
  }

  public void close() {
    LOGGER.info("OpenTelemetry close");
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
      LOGGER.info("Logging OpenTelemetry metrics for debug purpose: {}", Arrays.toString(metrics.toArray()));
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
