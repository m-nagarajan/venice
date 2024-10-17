package com.linkedin.venice.stats;

import com.linkedin.venice.utils.concurrent.VeniceConcurrentHashMap;
import io.tehuti.metrics.MetricsRepository;
import java.util.Map;


public abstract class AbstractVeniceAggStats<T extends AbstractVeniceStats> {
  public final static String STORE_NAME_FOR_TOTAL_STAT = "total";
  protected T totalStats;
  protected final Map<String, T> storeStats = new VeniceConcurrentHashMap<>();

  private StatsSupplier<T> statsFactory;
  private StatsSupplierNew<T> statsFactoryNew;

  private final MetricsRepository metricsRepository;

  private AbstractVeniceAggStats(MetricsRepository metricsRepository, StatsSupplier<T> statsSupplier, T totalStats) {
    this.metricsRepository = metricsRepository;
    this.statsFactory = statsSupplier;
    this.totalStats = totalStats;
  }

  private AbstractVeniceAggStats(
      VeniceMetricsRepository metricsRepository,
      StatsSupplierNew<T> statsSupplier,
      T totalStats) {
    this.metricsRepository = metricsRepository;
    this.statsFactoryNew = statsSupplier;
    this.totalStats = totalStats;
  }

  public AbstractVeniceAggStats(MetricsRepository metricsRepository, StatsSupplier<T> statsSupplier) {
    this(metricsRepository, statsSupplier, statsSupplier.get(metricsRepository, STORE_NAME_FOR_TOTAL_STAT, null));
  }

  public AbstractVeniceAggStats(
      VeniceMetricsRepository metricsRepository,
      StatsSupplierNew<T> statsSupplier,
      String dummy) {
    this(metricsRepository, statsSupplier, statsSupplier.get(metricsRepository, STORE_NAME_FOR_TOTAL_STAT, null));
  }

  public AbstractVeniceAggStats(MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
  }

  public void setStatsSupplier(StatsSupplierNew<T> statsSupplier) {
    this.statsFactoryNew = statsSupplier;
    if (metricsRepository instanceof VeniceMetricsRepository) {
      this.totalStats = statsSupplier.get((VeniceMetricsRepository) metricsRepository, STORE_NAME_FOR_TOTAL_STAT, null);
    } else {
      throw new RuntimeException("MetricsRepository is not an instance of VeniceMetricsRepository");
    }
  }

  public AbstractVeniceAggStats(
      String clusterName,
      MetricsRepository metricsRepository,
      StatsSupplier<T> statsSupplier) {
    this(
        metricsRepository,
        statsSupplier,
        statsSupplier.get(metricsRepository, STORE_NAME_FOR_TOTAL_STAT + "." + clusterName, null));
  }

  public T getStoreStats(String storeName) {
    if (metricsRepository instanceof VeniceMetricsRepository) {
      return storeStats.computeIfAbsent(
          storeName,
          k -> statsFactoryNew.get((VeniceMetricsRepository) metricsRepository, storeName, totalStats));
    } else {
      return storeStats.computeIfAbsent(storeName, k -> statsFactory.get(metricsRepository, storeName, totalStats));
    }
  }

  public T getTotalStats() {
    return totalStats;
  }
}
