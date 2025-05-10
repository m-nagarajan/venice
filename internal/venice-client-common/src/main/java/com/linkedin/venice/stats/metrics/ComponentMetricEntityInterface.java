package com.linkedin.venice.stats.metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Every venice component that defines its set of {@link MetricEntity} should implement this interface.
 */
public interface ComponentMetricEntityInterface {
  MetricEntity getMetricEntity();

  /**
   * Get all the unique {@link MetricEntity} from the provided enum classes based on the metric name.
   * This will also check if there are multiple metric entities with the same name across the provided
   * enum classes with different metric types or units and throw.
   */
  static Collection<MetricEntity> getUniqueMetricEntities(
      Class<? extends ComponentMetricEntityInterface>... enumClasses) {
    if (enumClasses == null || enumClasses.length == 0) {
      throw new IllegalArgumentException("Enum classes passed to getUniqueMetricEntities cannot be null or empty");
    }

    Map<String, MetricEntity> uniqueMetricsByName = new HashMap<>();
    for (Class<? extends ComponentMetricEntityInterface> enumClass: enumClasses) {
      ComponentMetricEntityInterface[] constants = enumClass.getEnumConstants();
      for (ComponentMetricEntityInterface constant: constants) {
        MetricEntity metric = constant.getMetricEntity();
        String metricName = metric.getMetricName();

        // Add only if not already present and validate if already present
        if (uniqueMetricsByName.containsKey(metricName)) {
          MetricEntity existingMetric = uniqueMetricsByName.get(metricName);
          if (!existingMetric.getMetricType().equals(metric.getMetricType())
              || !existingMetric.getUnit().equals(metric.getUnit())) {
            throw new IllegalArgumentException(
                "Multiple metric entities with the same name but different types or units found for metric : "
                    + metricName + " among the provided enum classes: " + Arrays.toString(enumClasses));
          }
        } else {
          uniqueMetricsByName.put(metricName, metric);
        }
      }
    }

    if (uniqueMetricsByName.isEmpty()) {
      throw new IllegalArgumentException("No metric entities found in the provided enum classes");
    }

    return uniqueMetricsByName.values();
  }

}
