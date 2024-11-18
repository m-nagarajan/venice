package com.linkedin.venice.stats;

import static com.linkedin.venice.stats.VeniceOpenTelemetryMetricNamingFormat.transformMetricName;
import static com.linkedin.venice.stats.VeniceOpenTelemetryMetricNamingFormat.validateMetricName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.HashMap;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class VeniceOpenTelemetryMetricsRepositoryTest {
  private VeniceOpenTelemetryMetricsRepository metricsRepository;

  private VeniceMetricsConfig mockMetricsConfig;

  @BeforeMethod
  public void setUp() {
    mockMetricsConfig = Mockito.mock(VeniceMetricsConfig.class);
    Mockito.when(mockMetricsConfig.emitOtelMetrics()).thenReturn(true);
    Mockito.when(mockMetricsConfig.getMetricNamingFormat())
        .thenReturn(VeniceOpenTelemetryMetricNamingFormat.SNAKE_CASE);
    Mockito.when(mockMetricsConfig.getMetricPrefix()).thenReturn("test_prefix");
    Mockito.when(mockMetricsConfig.getServiceName()).thenReturn("test_service");
    Mockito.when(mockMetricsConfig.exportOtelMetricsToEndpoint()).thenReturn(true);
    Mockito.when(mockMetricsConfig.getOtelEndpoint()).thenReturn("http://localhost:4318");

    metricsRepository = new VeniceOpenTelemetryMetricsRepository(mockMetricsConfig);
  }

  @AfterMethod
  public void tearDown() {
    metricsRepository.close();
  }

  @Test
  public void testConstructorInitialize() {
    // Check if OpenTelemetry and SdkMeterProvider are initialized correctly
    assertNotNull(metricsRepository.getSdkMeterProvider());
    assertNotNull(metricsRepository.getMeter());
  }

  @Test
  public void testConstructorWithEmitDisabled() {
    Mockito.when(mockMetricsConfig.emitOtelMetrics()).thenReturn(false);
    VeniceOpenTelemetryMetricsRepository metricsRepository =
        new VeniceOpenTelemetryMetricsRepository(mockMetricsConfig);

    // Verify that metrics-related fields are null when metrics are disabled
    assertNull(metricsRepository.getSdkMeterProvider());
    assertNull(metricsRepository.getMeter());
    assertNull(metricsRepository.getHistogram("test", "unit", "desc"));
    assertNull(metricsRepository.getCounter("test", "unit", "desc"));
  }

  @Test
  public void testGetOtlpHttpMetricExporterWithValidConfig() {
    HashMap<String, String> otelConfigs = new HashMap<>();
    otelConfigs.put("otel.exporter.otlp.endpoint", "http://localhost:4318");

    MetricExporter exporter = metricsRepository.getOtlpHttpMetricExporter(mockMetricsConfig);

    // Verify that the exporter is not null and is of the expected type
    assertNotNull(exporter);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValidateMetricNameWithNullName() {
    validateMetricName(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValidateMetricNameWithEmptyName() {
    validateMetricName("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValidateMetricNameWithInvalidName() {
    validateMetricName("Invalid Name!");
  }

  @Test
  public void testTransformMetricName() {
    Mockito.when(mockMetricsConfig.getMetricNamingFormat())
        .thenReturn(VeniceOpenTelemetryMetricNamingFormat.SNAKE_CASE);
    assertEquals(metricsRepository.getFullMetricName("prefix", "metric_name"), "prefix.metric_name");

    String transformedName =
        transformMetricName("test.test_metric_name", VeniceOpenTelemetryMetricNamingFormat.PASCAL_CASE);
    assertEquals(transformedName, "Test.TestMetricName");

    transformedName = transformMetricName("test.test_metric_name", VeniceOpenTelemetryMetricNamingFormat.CAMEL_CASE);
    assertEquals(transformedName, "test.testMetricName");
  }

  @Test
  public void testCreateTwoHistograms() {
    DoubleHistogram histogram1 = metricsRepository.getHistogram("test_histogram", "unit", "description");
    DoubleHistogram histogram2 = metricsRepository.getHistogram("test_histogram", "unit", "description");

    assertNotNull(histogram1);
    assertSame(histogram1, histogram2, "Should return the same instance for the same histogram name.");
  }

  @Test
  public void testCreateTwoCounters() {
    LongCounter counter1 = metricsRepository.getCounter("test_counter", "unit", "description");
    LongCounter counter2 = metricsRepository.getCounter("test_counter", "unit", "description");

    assertNotNull(counter1);
    assertSame(counter1, counter2, "Should return the same instance for the same counter name.");
  }
}
