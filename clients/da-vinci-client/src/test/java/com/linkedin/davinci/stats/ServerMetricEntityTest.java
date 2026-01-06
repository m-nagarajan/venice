package com.linkedin.davinci.stats;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.linkedin.venice.stats.dimensions.VeniceMetricsDimensions;
import com.linkedin.venice.stats.metrics.MetricEntity;
import com.linkedin.venice.stats.metrics.MetricType;
import com.linkedin.venice.stats.metrics.MetricUnit;
import com.linkedin.venice.utils.Utils;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;


public class ServerMetricEntityTest {
  @Test
  public void testServerMetricEntities() {
    Map<ServerMetricEntity, MetricEntity> expectedMetrics = new HashMap<>();
    expectedMetrics.put(
        ServerMetricEntity.INGESTION_HEARTBEAT_DELAY,
        new MetricEntity(
            "ingestion.replication.heartbeat.delay",
            MetricType.HISTOGRAM,
            MetricUnit.MILLISECOND,
            "Heartbeat replication heartbeat delay in milliseconds",
            Utils.setOf(
                VeniceMetricsDimensions.VENICE_STORE_NAME,
                VeniceMetricsDimensions.VENICE_CLUSTER_NAME,
                VeniceMetricsDimensions.VENICE_REGION_NAME,
                VeniceMetricsDimensions.VENICE_VERSION_TYPE,
                VeniceMetricsDimensions.VENICE_REPLICA_TYPE,
                VeniceMetricsDimensions.VENICE_REPLICA_STATE)));

    for (ServerMetricEntity metric: ServerMetricEntity.values()) {
      MetricEntity actual = metric.getMetricEntity();
      MetricEntity expected = expectedMetrics.get(metric);

      assertNotNull(expected, "No expected definition for " + metric.name());
      assertNotNull(actual.getMetricName(), "Metric name should not be null for " + metric.name());
      assertEquals(actual.getMetricName(), expected.getMetricName(), "Unexpected metric name for " + metric.name());
      assertNotNull(actual.getMetricType(), "Metric type should not be null for " + metric.name());
      assertEquals(actual.getMetricType(), expected.getMetricType(), "Unexpected metric type for " + metric.name());
      assertNotNull(actual.getUnit(), "Metric unit should not be null for " + metric.name());
      assertEquals(actual.getUnit(), expected.getUnit(), "Unexpected metric unit for " + metric.name());
      assertNotNull(actual.getDescription(), "Metric description should not be null for " + metric.name());
      assertEquals(
          actual.getDescription(),
          expected.getDescription(),
          "Unexpected metric description for " + metric.name());
      assertNotNull(actual.getDimensionsList(), "Metric dimensions should not be null for " + metric.name());
      assertEquals(
          actual.getDimensionsList(),
          expected.getDimensionsList(),
          "Unexpected metric dimensions for " + metric.name());
    }
  }

  @Test
  public void testIngestionHeartbeatDelayMetricEntity() {
    ServerMetricEntity entity = ServerMetricEntity.INGESTION_HEARTBEAT_DELAY;
    MetricEntity metricEntity = entity.getMetricEntity();

    // Verify metric name
    assertEquals(
        metricEntity.getMetricName(),
        "ingestion.replication.heartbeat.delay",
        "Incorrect metric name for INGESTION_HEARTBEAT_DELAY");

    // Verify metric type
    assertEquals(metricEntity.getMetricType(), MetricType.HISTOGRAM, "INGESTION_HEARTBEAT_DELAY should be a HISTOGRAM");

    // Verify metric unit
    assertEquals(
        metricEntity.getUnit(),
        MetricUnit.MILLISECOND,
        "INGESTION_HEARTBEAT_DELAY should use MILLISECOND unit");

    // Verify description is not empty
    assertNotNull(metricEntity.getDescription(), "Description should not be null");
    assertEquals(
        metricEntity.getDescription(),
        "Heartbeat replication heartbeat delay in milliseconds",
        "Incorrect description");

    // Verify dimensions list contains all expected dimensions
    assertEquals(metricEntity.getDimensionsList().size(), 6, "Should have 6 dimensions");
    assertEquals(
        metricEntity.getDimensionsList(),
        Utils.setOf(
            VeniceMetricsDimensions.VENICE_STORE_NAME,
            VeniceMetricsDimensions.VENICE_CLUSTER_NAME,
            VeniceMetricsDimensions.VENICE_REGION_NAME,
            VeniceMetricsDimensions.VENICE_VERSION_TYPE,
            VeniceMetricsDimensions.VENICE_REPLICA_TYPE,
            VeniceMetricsDimensions.VENICE_REPLICA_STATE),
        "Dimensions list does not match expected");
  }

  @Test
  public void testEnumValuesCount() {
    // Verify we have the expected number of metrics
    // Update this number as new metrics are added
    assertEquals(ServerMetricEntity.values().length, 1, "Expected 1 ServerMetricEntity enum value");
  }

  @Test
  public void testGetMetricEntityNotNull() {
    // Verify that getMetricEntity() never returns null for any enum value
    for (ServerMetricEntity entity: ServerMetricEntity.values()) {
      assertNotNull(entity.getMetricEntity(), "getMetricEntity() should not return null for " + entity.name());
    }
  }
}
