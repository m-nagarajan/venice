package com.linkedin.venice.stats;

import com.linkedin.venice.utils.concurrent.VeniceConcurrentHashMap;
import io.tehuti.metrics.MetricsRepository;
import java.util.Map;


public abstract class AbstractVeniceAggStats<T extends AbstractVeniceStats> {
  public final static String STORE_NAME_FOR_TOTAL_STAT = "total";
  protected T totalStats;
  protected final Map<String, T> storeStats = new VeniceConcurrentHashMap<>();

  private StatsSupplier<T> statsFactory;

  private final MetricsRepository metricsRepository;
  private String clusterName = null;

  private AbstractVeniceAggStats(
      String clusterName,
      MetricsRepository metricsRepository,
      StatsSupplier<T> statsSupplier,
      T totalStats) {
    this.clusterName = clusterName;
    this.metricsRepository = metricsRepository;
    this.statsFactory = statsSupplier;
    this.totalStats = totalStats;
  }

  public AbstractVeniceAggStats(String clusterName, MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
    this.clusterName = clusterName;
  }

  public void setStatsSupplier(StatsSupplier<T> statsSupplier) {
    this.statsFactory = statsSupplier;
    this.totalStats = statsSupplier.get(metricsRepository, STORE_NAME_FOR_TOTAL_STAT, clusterName, null);
  }

  public AbstractVeniceAggStats(
      String clusterName,
      MetricsRepository metricsRepository,
      StatsSupplier<T> statsSupplier,
      boolean perClusterAggregate) {
    this(
        clusterName,
        metricsRepository,
        statsSupplier,
        statsSupplier.get(
            metricsRepository,
            perClusterAggregate ? STORE_NAME_FOR_TOTAL_STAT + "." + clusterName : STORE_NAME_FOR_TOTAL_STAT,
            clusterName,
            null));
  }

  public T getStoreStats(String storeName) {
    return storeStats
        .computeIfAbsent(storeName, k -> statsFactory.get(metricsRepository, storeName, clusterName, totalStats));
  }

  public T getNullableStoreStats(String storeName) {
    return storeStats.get(storeName);
  }

  public T getTotalStats() {
    return totalStats;
  }
}
