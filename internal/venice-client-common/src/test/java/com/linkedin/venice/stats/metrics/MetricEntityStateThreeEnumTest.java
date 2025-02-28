package com.linkedin.venice.stats.metrics;

import static com.linkedin.venice.read.RequestType.MULTI_GET_STREAMING;
import static com.linkedin.venice.stats.VeniceOpenTelemetryMetricNamingFormat.getDefaultFormat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.stats.VeniceMetricsConfig;
import com.linkedin.venice.stats.VeniceOpenTelemetryMetricsRepository;
import com.linkedin.venice.stats.dimensions.VeniceMetricsDimensions;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MetricEntityStateThreeEnumTest {
  private VeniceOpenTelemetryMetricsRepository mockOtelRepository;
  private MetricEntity mockMetricEntity;
  private Map<VeniceMetricsDimensions, String> baseDimensionsMap;
  private final Map<String, Attributes> attributesMap = new HashMap<>();

  @BeforeMethod
  public void setUp() {
    mockOtelRepository = Mockito.mock(VeniceOpenTelemetryMetricsRepository.class);
    when(mockOtelRepository.emitOpenTelemetryMetrics()).thenReturn(true);
    when(mockOtelRepository.getMetricFormat()).thenReturn(getDefaultFormat());
    when(mockOtelRepository.getDimensionName(any())).thenCallRealMethod();
    when(mockOtelRepository.createAttributes(any(), any(), any())).thenCallRealMethod();
    VeniceMetricsConfig mockMetricsConfig = Mockito.mock(VeniceMetricsConfig.class);
    when(mockMetricsConfig.getOtelCustomDimensionsMap()).thenReturn(new HashMap<>());
    when(mockOtelRepository.getMetricsConfig()).thenReturn(mockMetricsConfig);
    mockMetricEntity = Mockito.mock(MetricEntity.class);
    Set<VeniceMetricsDimensions> dimensionsSet = new HashSet<>();
    dimensionsSet.add(VeniceMetricsDimensions.VENICE_REQUEST_METHOD);
    dimensionsSet.add(MetricEntityStateTest.DimensionEnum1.DIMENSION_ONE.getDimensionName());
    dimensionsSet.add(MetricEntityStateTest.DimensionEnum2.DIMENSION_ONE.getDimensionName());
    dimensionsSet.add(MetricEntityStateTest.DimensionEnum3.DIMENSION_ONE.getDimensionName());
    doReturn(dimensionsSet).when(mockMetricEntity).getDimensionsList();
    baseDimensionsMap = new HashMap<>();
    baseDimensionsMap.put(VeniceMetricsDimensions.VENICE_REQUEST_METHOD, MULTI_GET_STREAMING.getDimensionValue());

    for (MetricEntityStateTest.DimensionEnum1 enum1: MetricEntityStateTest.DimensionEnum1.values()) {
      for (MetricEntityStateTest.DimensionEnum2 enum2: MetricEntityStateTest.DimensionEnum2.values()) {
        for (MetricEntityStateTest.DimensionEnum3 enum3: MetricEntityStateTest.DimensionEnum3.values()) {
          AttributesBuilder attributesBuilder = Attributes.builder();
          for (Map.Entry<VeniceMetricsDimensions, String> entry: baseDimensionsMap.entrySet()) {
            attributesBuilder.put(mockOtelRepository.getDimensionName(entry.getKey()), entry.getValue());
          }
          attributesBuilder
              .put(mockOtelRepository.getDimensionName(enum1.getDimensionName()), enum1.getDimensionValue());
          attributesBuilder
              .put(mockOtelRepository.getDimensionName(enum2.getDimensionName()), enum2.getDimensionValue());
          attributesBuilder
              .put(mockOtelRepository.getDimensionName(enum3.getDimensionName()), enum3.getDimensionValue());
          Attributes attributes = attributesBuilder.build();
          String attributeName =
              String.format("attributesDimensionEnum1%sEnum2%sEnum3%s", enum1.name(), enum2.name(), enum3.name());
          attributesMap.put(attributeName, attributes);
        }
      }
    }
  }

  @Test
  public void testConstructorWithoutOtelRepo() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.DimensionEnum1, MetricEntityStateTest.DimensionEnum2, MetricEntityStateTest.DimensionEnum3> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            null,
            baseDimensionsMap,
            MetricEntityStateTest.DimensionEnum1.class,
            MetricEntityStateTest.DimensionEnum2.class,
            MetricEntityStateTest.DimensionEnum3.class);
    assertNotNull(metricEntityState);
    assertEquals(metricEntityState.getAttributesEnumMap().size(), 0);
    for (MetricEntityStateTest.DimensionEnum1 enum1: MetricEntityStateTest.DimensionEnum1.values()) {
      for (MetricEntityStateTest.DimensionEnum2 enum2: MetricEntityStateTest.DimensionEnum2.values()) {
        for (MetricEntityStateTest.DimensionEnum3 enum3: MetricEntityStateTest.DimensionEnum3.values()) {
          assertNull(metricEntityState.getAttributes(enum1, enum2, enum3));
        }
      }
    }
  }

  @Test
  public void testConstructorWithOtelRepo() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.DimensionEnum1, MetricEntityStateTest.DimensionEnum2, MetricEntityStateTest.DimensionEnum3> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            mockOtelRepository,
            baseDimensionsMap,
            MetricEntityStateTest.DimensionEnum1.class,
            MetricEntityStateTest.DimensionEnum2.class,
            MetricEntityStateTest.DimensionEnum3.class);
    assertNotNull(metricEntityState);
    assertEquals(metricEntityState.getAttributesEnumMap().size(), 2); // MetricEntityStateTest.DimensionEnum1 length

    for (Map.Entry<String, Attributes> entry: attributesMap.entrySet()) {
      String attributeName = entry.getKey();
      Attributes expectedAttributes = entry.getValue();

      String enum1Name = attributeName.substring("attributesDimensionEnum1".length(), attributeName.indexOf("Enum2"));
      String enum2Name =
          attributeName.substring(attributeName.indexOf("Enum2") + "Enum2".length(), attributeName.indexOf("Enum3"));
      String enum3Name = attributeName.substring(attributeName.indexOf("Enum3") + "Enum3".length());

      Attributes actualAttributes = metricEntityState.getAttributes(
          MetricEntityStateTest.DimensionEnum1.valueOf(enum1Name),
          MetricEntityStateTest.DimensionEnum2.valueOf(enum2Name),
          MetricEntityStateTest.DimensionEnum3.valueOf(enum3Name));

      assertNotNull(actualAttributes);
      assertEquals(actualAttributes.size(), 4);
      assertEquals(actualAttributes, expectedAttributes);
    }
  }

  @Test(expectedExceptions = VeniceException.class, expectedExceptionsMessageRegExp = "The dimensions map is empty. Please check the enum types and ensure they are properly defined.")
  public void testCreateAttributesEnumMapWithEmptyEnum() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.EmptyDimensionEnum, MetricEntityStateTest.EmptyDimensionEnum, MetricEntityStateTest.EmptyDimensionEnum> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            mockOtelRepository,
            baseDimensionsMap,
            MetricEntityStateTest.EmptyDimensionEnum.class,
            MetricEntityStateTest.EmptyDimensionEnum.class,
            MetricEntityStateTest.EmptyDimensionEnum.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "The key for otel dimension cannot be null.*")
  public void testGetAttributesWithNullKey() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.DimensionEnum1, MetricEntityStateTest.DimensionEnum2, MetricEntityStateTest.DimensionEnum3> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            mockOtelRepository,
            baseDimensionsMap,
            MetricEntityStateTest.DimensionEnum1.class,
            MetricEntityStateTest.DimensionEnum2.class,
            MetricEntityStateTest.DimensionEnum3.class);
    metricEntityState.getAttributes(null, null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "The key for otel dimension is not of the correct type.*")
  public void testGetAttributesWithInvalidKeyType() {
    MetricEntityStateThreeEnums metricEntityState = MetricEntityStateThreeEnums.create(
        mockMetricEntity,
        mockOtelRepository,
        baseDimensionsMap,
        MetricEntityStateTest.DimensionEnum1.class,
        MetricEntityStateTest.DimensionEnum2.class,
        MetricEntityStateTest.DimensionEnum3.class);
    metricEntityState.getAttributes(MULTI_GET_STREAMING, MULTI_GET_STREAMING, MULTI_GET_STREAMING);
  }

  @Test
  public void testRecordWithValidKey() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.DimensionEnum1, MetricEntityStateTest.DimensionEnum2, MetricEntityStateTest.DimensionEnum3> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            mockOtelRepository,
            baseDimensionsMap,
            MetricEntityStateTest.DimensionEnum1.class,
            MetricEntityStateTest.DimensionEnum2.class,
            MetricEntityStateTest.DimensionEnum3.class);
    metricEntityState.record(
        100L,
        MetricEntityStateTest.DimensionEnum1.DIMENSION_ONE,
        MetricEntityStateTest.DimensionEnum2.DIMENSION_ONE,
        MetricEntityStateTest.DimensionEnum3.DIMENSION_ONE);
    metricEntityState.record(
        100.5,
        MetricEntityStateTest.DimensionEnum1.DIMENSION_ONE,
        MetricEntityStateTest.DimensionEnum2.DIMENSION_ONE,
        MetricEntityStateTest.DimensionEnum3.DIMENSION_TWO);
    // No exception expected
  }

  @Test
  public void testRecordWithNullKey() {
    MetricEntityStateThreeEnums<MetricEntityStateTest.DimensionEnum1, MetricEntityStateTest.DimensionEnum2, MetricEntityStateTest.DimensionEnum3> metricEntityState =
        MetricEntityStateThreeEnums.create(
            mockMetricEntity,
            mockOtelRepository,
            baseDimensionsMap,
            MetricEntityStateTest.DimensionEnum1.class,
            MetricEntityStateTest.DimensionEnum2.class,
            MetricEntityStateTest.DimensionEnum3.class);
    // Null key will cause IllegalArgumentException in getDimension, record should catch it.
    metricEntityState.record(100L, null, null, null);
    metricEntityState.record(100.5, null, null, null);
  }
}
