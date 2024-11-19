package com.linkedin.venice.stats;

import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_BUCKETS;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_SCALE;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_ENDPOINT;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_PROTOCOL;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_VENICE_ENABLED;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_VENICE_EXPORT_TO_ENDPOINT;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_VENICE_EXPORT_TO_LOG;
import static com.linkedin.venice.stats.VeniceMetricsConfig.OTEL_VENICE_METRICS_NAMING_FORMAT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.linkedin.venice.stats.VeniceMetricsConfig.Builder;
import io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.tehuti.metrics.MetricConfig;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;


public class VeniceMetricsConfigTest {
  @Test
  public void testDefaultValues() {
    new Builder().build();
  }

  @Test
  public void testDefaultValuesWithBasicConfig() {
    VeniceMetricsConfig config = new Builder().setServiceName("noop_service").setMetricPrefix("service").build();
    assertEquals(config.getServiceName(), "noop_service");
    assertEquals(config.getMetricPrefix(), "service");
    assertFalse(config.emitOtelMetrics());
    assertFalse(config.exportOtelMetricsToEndpoint());
    assertEquals(config.getOtelExportProtocol(), OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    assertEquals(config.getOtelEndpoint(), null);
    assertTrue(config.getOtelHeaders().isEmpty());
    assertFalse(config.exportOtelMetricsToLog());
    assertEquals(config.getMetricNamingFormat(), VeniceOpenTelemetryMetricNamingFormat.SNAKE_CASE);
    assertEquals(config.getOtelAggregationTemporalitySelector(), AggregationTemporalitySelector.deltaPreferred());
    assertEquals(config.getOtelHistogramAggregationSelector(), null);
    assertNotNull(config.getTehutiMetricConfig());
  }

  @Test
  public void testCustomValues() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_LOG, "true");

    MetricConfig metricConfig = new MetricConfig();

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .setTehutiMetricConfig(metricConfig)
        .extractAndSetOtelConfigs(otelConfigs)
        .build();

    assertEquals(config.getServiceName(), "TestService");
    assertEquals(config.getMetricPrefix(), "TestPrefix");
    assertTrue(config.emitOtelMetrics());
    assertTrue(config.exportOtelMetricsToLog());
    assertEquals(config.getTehutiMetricConfig(), metricConfig);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOtelMissingConfigs() {
    Map<String, String> invalidOtelConfigs = new HashMap<>();
    invalidOtelConfigs.put(OTEL_VENICE_ENABLED, "true");
    invalidOtelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");

    new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(invalidOtelConfigs)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOtelConfigWithInvalidMetricFormat() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_METRICS_NAMING_FORMAT, "INVALID_FORMAT");

    new Builder().extractAndSetOtelConfigs(otelConfigs).build();
  }

  @Test
  public void testOtelConfigWithValidMetricFormat() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_METRICS_NAMING_FORMAT, "CAMEL_CASE");

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();

    assertEquals(config.getMetricNamingFormat(), VeniceOpenTelemetryMetricNamingFormat.CAMEL_CASE);
  }

  @Test
  public void testEnableHttpGrpcEndpointConfigWithRequiredFields() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, "http://localhost");

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();

    assertTrue(config.exportOtelMetricsToEndpoint());
    assertEquals(config.getOtelExportProtocol(), OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    assertEquals(config.getOtelEndpoint(), "http://localhost");
  }

  @Test
  public void testSetAggregationTemporalitySelector() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, "http://localhost");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE, "delta");

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();
    assertEquals(config.getOtelAggregationTemporalitySelector(), AggregationTemporalitySelector.deltaPreferred());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetAggregationTemporalitySelectorInvalidConfig() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, "http://localhost");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE, "invalid");

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();
    assertEquals(config.getOtelAggregationTemporalitySelector(), AggregationTemporalitySelector.deltaPreferred());
  }

  @Test
  public void testSetHistogramAggregationSelector() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, "http://localhost");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION, "base2_exponential_bucket_histogram");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_SCALE, "10");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_BUCKETS, "50");

    VeniceMetricsConfig config = new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();
    assertEquals(
        config.getOtelHistogramAggregationSelector().getDefaultAggregation(InstrumentType.HISTOGRAM).toString(),
        "Base2ExponentialHistogramAggregation{maxBuckets=50,maxScale=10}");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetHistogramAggregationSelectorInvalidConfig() {
    Map<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put(OTEL_VENICE_ENABLED, "true");
    otelConfigs.put(OTEL_VENICE_EXPORT_TO_ENDPOINT, "true");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF);
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, "http://localhost");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION, "invalid");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_SCALE, "10");
    otelConfigs.put(OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION_MAX_BUCKETS, "50");

    new Builder().setServiceName("TestService")
        .setMetricPrefix("TestPrefix")
        .extractAndSetOtelConfigs(otelConfigs)
        .build();
  }
}
