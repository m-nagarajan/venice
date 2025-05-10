package com.linkedin.venice.stats.metrics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.linkedin.venice.stats.dimensions.VeniceMetricsDimensions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.Test;


public class ComponentMetricEntityInterfaceTest {
  private static final Set<VeniceMetricsDimensions> DUMMY_DIMENSIONS =
      Collections.<VeniceMetricsDimensions>singleton(null);
  private static final String DESC = "dummy";

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Enum classes passed to getUniqueMetricEntities cannot be null or empty")
  public void testNullVarargs() {
    ComponentMetricEntityInterface.getUniqueMetricEntities((Class<? extends ComponentMetricEntityInterface>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Enum classes passed to getUniqueMetricEntities cannot be null or empty")
  public void testEmptyVarargs() {
    ComponentMetricEntityInterface.getUniqueMetricEntities();
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "No metric entities found in the provided enum classes")
  public void testNoMetricsFound() {
    ComponentMetricEntityInterface.getUniqueMetricEntities(EmptyEnum.class);
  }

  @Test
  public void testSingleEnum() {
    Collection<MetricEntity> metrics = ComponentMetricEntityInterface.getUniqueMetricEntities(SingleEnum.class);

    assertEquals(metrics.size(), 1);
    MetricEntity m = metrics.iterator().next();
    assertEquals(m.getMetricName(), "metric1");
    assertEquals(m.getMetricType(), MetricType.COUNTER);
    assertEquals(m.getUnit(), MetricUnit.NUMBER);
  }

  @Test
  public void testDistinctEnums() {
    Collection<MetricEntity> metrics = ComponentMetricEntityInterface.getUniqueMetricEntities(TwoDistinctEnum.class);

    assertEquals(metrics.size(), 2);
    Set<String> names = new HashSet<>();
    for (MetricEntity m: metrics) {
      names.add(m.getMetricName());
    }
    assertTrue(names.contains("mA"));
    assertTrue(names.contains("mB"));
  }

  @Test
  public void testDuplicateMetricsSameTypeAndUnit() {
    Collection<MetricEntity> metrics =
        ComponentMetricEntityInterface.getUniqueMetricEntities(DuplicateEnumOne.class, DuplicateEnumTwo.class);

    // only one unique name “dup”
    assertEquals(metrics.size(), 1);

    // and it should be the instance from DuplicateEnumOne.A
    MetricEntity only = metrics.iterator().next();
    assertTrue(only == DuplicateEnumOne.A.getMetricEntity());
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Multiple metric entities with the same name but different types or units.*")
  public void testConflictMetricsDifferentUnit() {
    ComponentMetricEntityInterface.getUniqueMetricEntities(ConflictEnum.class, ConflictEnumDifferentUnit.class);
  }

  /** No constants: triggers “no metrics found” */
  private enum EmptyEnum implements ComponentMetricEntityInterface {
    ;
    @Override
    public MetricEntity getMetricEntity() {
      return null;
    }
  }

  /** Single constant with one MetricEntity */
  private enum SingleEnum implements ComponentMetricEntityInterface {
    A;

    private final MetricEntity metric =
        new MetricEntity("metric1", MetricType.COUNTER, MetricUnit.NUMBER, DESC, DUMMY_DIMENSIONS);

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }

  /** Two distinct metric names */
  private enum TwoDistinctEnum implements ComponentMetricEntityInterface {
    A(new MetricEntity("mA", MetricType.COUNTER, MetricUnit.NUMBER, DESC, DUMMY_DIMENSIONS)),
    B(new MetricEntity("mB", MetricType.HISTOGRAM, MetricUnit.MILLISECOND, DESC, DUMMY_DIMENSIONS));

    private final MetricEntity metric;

    TwoDistinctEnum(MetricEntity m) {
      this.metric = m;
    }

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }

  /** Duplicate name, same type & unit: should be deduped */
  private enum DuplicateEnumOne implements ComponentMetricEntityInterface {
    A;

    private final MetricEntity metric =
        new MetricEntity("dup", MetricType.COUNTER, MetricUnit.NUMBER, DESC, DUMMY_DIMENSIONS);

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }

  private enum DuplicateEnumTwo implements ComponentMetricEntityInterface {
    B;

    private final MetricEntity metric =
        new MetricEntity("dup", MetricType.COUNTER, MetricUnit.NUMBER, DESC, DUMMY_DIMENSIONS);

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }

  /** Duplicate name, different type: should throw */
  private enum ConflictEnum implements ComponentMetricEntityInterface {
    X;

    private final MetricEntity metric =
        new MetricEntity("conflict", MetricType.COUNTER, MetricUnit.NUMBER, DESC, DUMMY_DIMENSIONS);

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }

  private enum ConflictEnumDifferentUnit implements ComponentMetricEntityInterface {
    Y;

    private final MetricEntity metric =
        new MetricEntity("conflict", MetricType.HISTOGRAM, MetricUnit.MILLISECOND, DESC, DUMMY_DIMENSIONS);

    @Override
    public MetricEntity getMetricEntity() {
      return metric;
    }
  }
}
