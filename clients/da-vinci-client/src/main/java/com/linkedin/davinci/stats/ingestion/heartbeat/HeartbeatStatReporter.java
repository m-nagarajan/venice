package com.linkedin.davinci.stats.ingestion.heartbeat;

import static com.linkedin.venice.stats.StatsErrorCode.NULL_INGESTION_STATS;

import com.linkedin.davinci.stats.AbstractVeniceStatsReporter;
import io.tehuti.metrics.MetricsRepository;
import io.tehuti.metrics.stats.AsyncGauge;
import java.util.Set;


public class HeartbeatStatReporter extends AbstractVeniceStatsReporter<HeartbeatStat> {
  private static final String LEADER_METRIC_PREFIX = "heartbeat_delay_ms_leader-";
  private static final String FOLLOWER_METRIC_PREFIX = "heartbeat_delay_ms_follower-";
  private static final String CATCHUP_UP_FOLLOWER_METRIC_PREFIX = "catching_up_heartbeat_delay_ms_follower-";
  private static final String MAX = "-Max";
  private static final String AVG = "-Avg";

  public HeartbeatStatReporter(MetricsRepository metricsRepository, String storeName, Set<String> regions) {
    super(metricsRepository, storeName);
    for (String region: regions) {
      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }
        return getStats().getReadyToServeLeaderLag(region).getMax();
      }, LEADER_METRIC_PREFIX + region + MAX));

      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }

        return getStats().getReadyToServeFollowerLag(region).getMax();
      }, FOLLOWER_METRIC_PREFIX + region + MAX));

      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }

        return getStats().getReadyToServeLeaderLag(region).getAvg();
      }, LEADER_METRIC_PREFIX + region + AVG));

      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }

        return getStats().getReadyToServeFollowerLag(region).getAvg();
      }, FOLLOWER_METRIC_PREFIX + region + AVG));

      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }

        return getStats().getCatchingUpFollowerLag(region).getMax();
      }, CATCHUP_UP_FOLLOWER_METRIC_PREFIX + region + MAX));

      registerSensor(new AsyncGauge((ignored, ignored2) -> {
        if (getStats() == null) {
          return NULL_INGESTION_STATS.code;
        }

        return getStats().getCatchingUpFollowerLag(region).getAvg();
      }, CATCHUP_UP_FOLLOWER_METRIC_PREFIX + region + AVG));
    }
  }

  @Override
  protected void registerStats() {
    // NoOp
  }
}
