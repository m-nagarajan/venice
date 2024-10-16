package com.linkedin.venice.router.stats;

import static com.linkedin.venice.stats.AbstractVeniceAggStats.STORE_NAME_FOR_TOTAL_STAT;

import com.linkedin.alpini.router.monitoring.ScatterGatherStats;
import com.linkedin.venice.common.VeniceSystemStoreUtils;
import com.linkedin.venice.read.RequestType;
import com.linkedin.venice.stats.AbstractVeniceHttpStats;
import com.linkedin.venice.stats.LambdaStat;
import com.linkedin.venice.stats.TehutiUtils;
import com.linkedin.venice.stats.VeniceMetricsConfig;
import com.linkedin.venice.stats.VeniceMetricsRepository;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.tehuti.Metric;
import io.tehuti.metrics.MeasurableStat;
import io.tehuti.metrics.MetricConfig;
import io.tehuti.metrics.Sensor;
import io.tehuti.metrics.stats.Avg;
import io.tehuti.metrics.stats.Count;
import io.tehuti.metrics.stats.Gauge;
import io.tehuti.metrics.stats.Max;
import io.tehuti.metrics.stats.Min;
import io.tehuti.metrics.stats.OccurrenceRate;
import io.tehuti.metrics.stats.Rate;
import io.tehuti.metrics.stats.Total;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RouterHttpRequestStats extends AbstractVeniceHttpStats {
  private static final MetricConfig METRIC_CONFIG = new MetricConfig().timeWindow(10, TimeUnit.SECONDS);
  private static final VeniceMetricsRepository localMetricRepo = new VeniceMetricsRepository(
      new VeniceMetricsConfig.VeniceMetricsConfigBuilder().setTehutiMetricConfig(METRIC_CONFIG).build());
  private final static Sensor totalInflightRequestSensor = localMetricRepo.sensor("total_inflight_request");
  static {
    totalInflightRequestSensor.add("total_inflight_request_count", new Rate());
  }
  private final Sensor requestSensor;
  private final LongCounter requestSensorOtel;
  private final Sensor healthySensor;
  private final LongCounter healthySensorOtel;
  private final Sensor unhealthySensor;
  private final LongCounter unhealthySensorOtel;
  private final Sensor tardySensor;
  private final LongCounter tardySensorOtel;
  // response status based metrics
  private final Sensor healthyRequestRateSensor;
  private final Sensor tardyRequestRatioSensor;
  private final Sensor throttleSensor;
  private final Sensor errorRetryCountSensor;

  private final Sensor latencySensor;
  private final DoubleHistogram latencySensorOtel;
  private final Sensor healthyRequestLatencySensor;
  private final DoubleHistogram healthyRequestLatencySensorOtel;
  private final Sensor unhealthyRequestLatencySensor;
  private final DoubleHistogram unhealthyRequestLatencySensorOtel;
  private final Sensor tardyRequestLatencySensor;
  private final DoubleHistogram tardyRequestLatencySensorOtel;
  private final Sensor throttledRequestLatencySensor;
  private final Sensor requestSizeSensor;
  private final Sensor compressedResponseSizeSensor;
  private final Sensor responseSizeSensor;
  private final Sensor badRequestSensor;
  private final Sensor badRequestKeyCountSensor;
  private final Sensor requestThrottledByRouterCapacitySensor;
  private final Sensor decompressionTimeSensor;
  private final Sensor routerResponseWaitingTimeSensor;
  private final Sensor fanoutRequestCountSensor;
  private final Sensor quotaSensor;
  private final Sensor findUnhealthyHostRequestSensor;
  private final Sensor keyNumSensor;
  // Reflect the real request usage, e.g count each key as an unit of request usage.
  private final Sensor requestUsageSensor;
  private final Sensor requestParsingLatencySensor;
  private final Sensor requestRoutingLatencySensor;
  private final Sensor unAvailableRequestSensor;
  private final Sensor delayConstraintAbortedRetryRequest;
  private final Sensor slowRouteAbortedRetryRequest;
  private final Sensor retryRouteLimitAbortedRetryRequest;
  private final Sensor noAvailableReplicaAbortedRetryRequest;
  private final Sensor readQuotaUsageSensor;
  private final Sensor inFlightRequestSensor;
  private final AtomicInteger currentInFlightRequest;
  private final Sensor unavailableReplicaStreamingRequestSensor;
  private final Sensor allowedRetryRequestSensor;
  private final Sensor disallowedRetryRequestSensor;
  private final Sensor errorRetryAttemptTriggeredByPendingRequestCheckSensor;
  private final Sensor retryDelaySensor;
  private final Sensor multiGetFallbackSensor;
  private final Sensor metaStoreShadowReadSensor;
  private Sensor keySizeSensor;
  private final String systemStoreName;
  private final Attributes metricDimensions;

  // QPS metrics
  public RouterHttpRequestStats(
      VeniceMetricsRepository metricsRepository,
      String storeName,
      RequestType requestType,
      ScatterGatherStats scatterGatherStats,
      boolean isKeyValueProfilingEnabled) {
    super(metricsRepository, storeName, requestType);
    metricDimensions =
        Attributes.builder().put("storeName", storeName).put("requestType", requestType.toString()).build();

    this.systemStoreName = VeniceSystemStoreUtils.extractSystemStoreType(storeName);
    Rate requestRate = new OccurrenceRate();
    Rate healthyRequestRate = new OccurrenceRate();
    Rate tardyRequestRate = new OccurrenceRate();
    requestSensor = registerSensor("request", new Count(), requestRate);
    requestSensorOtel =
        metricsRepository.getOpenTelemetryMetricsRepository().getCounter("CallCount", "NUMBER", "All requests count");
    healthySensor = registerSensor("healthy_request", new Count(), healthyRequestRate);
    healthySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getCounter("SuccessCount", "NUMBER", "All healthy requests count");
    unhealthySensor = registerSensor("unhealthy_request", new Count());
    unhealthySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getCounter("ErrorCount", "NUMBER", "All unhealthy requests count");
    unavailableReplicaStreamingRequestSensor = registerSensor("unavailable_replica_streaming_request", new Count());
    tardySensor = registerSensor("tardy_request", new Count(), tardyRequestRate);
    tardySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getCounter("TardyCount", "NUMBER", "All tardy requests count");
    healthyRequestRateSensor =
        registerSensor(new TehutiUtils.SimpleRatioStat(healthyRequestRate, requestRate, "healthy_request_ratio"));
    tardyRequestRatioSensor =
        registerSensor(new TehutiUtils.SimpleRatioStat(tardyRequestRate, requestRate, "tardy_request_ratio"));
    throttleSensor = registerSensor("throttled_request", new Count());
    errorRetryCountSensor = registerSensor("error_retry", new Count());
    badRequestSensor = registerSensor("bad_request", new Count());
    badRequestKeyCountSensor = registerSensor("bad_request_key_count", new OccurrenceRate(), new Avg(), new Max());
    requestThrottledByRouterCapacitySensor = registerSensor("request_throttled_by_router_capacity", new Count());
    fanoutRequestCountSensor = registerSensor("fanout_request_count", new Avg(), new Max(0));
    latencySensor = registerSensorWithDetailedPercentiles("latency", new Avg(), new Max(0));
    latencySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getHistogram("CallTime", TimeUnit.MILLISECONDS.toString(), "Latency of all requests");

    healthyRequestLatencySensor =
        registerSensorWithDetailedPercentiles("healthy_request_latency", new Avg(), new Max(0));
    healthyRequestLatencySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getHistogram("SuccessTime", TimeUnit.MILLISECONDS.toString(), "Latency of all healthy requests");
    unhealthyRequestLatencySensor =
        registerSensorWithDetailedPercentiles("unhealthy_request_latency", new Avg(), new Max(0));
    unhealthyRequestLatencySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getHistogram("ErrorTime", TimeUnit.MILLISECONDS.toString(), "Latency of all unhealthy requests");
    tardyRequestLatencySensor = registerSensorWithDetailedPercentiles("tardy_request_latency", new Avg(), new Max(0));
    tardyRequestLatencySensorOtel = metricsRepository.getOpenTelemetryMetricsRepository()
        .getHistogram("TardyTime", TimeUnit.MILLISECONDS.toString(), "Latency of all tardy requests");
    throttledRequestLatencySensor =
        registerSensorWithDetailedPercentiles("throttled_request_latency", new Avg(), new Max(0));
    routerResponseWaitingTimeSensor = registerSensor(
        "response_waiting_time",
        TehutiUtils.getPercentileStat(getName(), getFullMetricName("response_waiting_time")));
    requestSizeSensor = registerSensor(
        "request_size",
        TehutiUtils.getPercentileStat(getName(), getFullMetricName("request_size")),
        new Avg());
    compressedResponseSizeSensor = registerSensor(
        "compressed_response_size",
        TehutiUtils.getPercentileStat(getName(), getFullMetricName("compressed_response_size")),
        new Avg(),
        new Max());

    decompressionTimeSensor = registerSensor(
        "decompression_time",
        TehutiUtils.getPercentileStat(getName(), getFullMetricName("decompression_time")),
        new Avg());
    quotaSensor = registerSensor("read_quota_per_router", new Gauge());
    findUnhealthyHostRequestSensor = registerSensor("find_unhealthy_host_request", new OccurrenceRate());

    registerSensor(new LambdaStat((ignored, ignored2) -> scatterGatherStats.getTotalRetries(), "retry_count"));
    registerSensor(new LambdaStat((ignored, ignored2) -> scatterGatherStats.getTotalRetriedKeys(), "retry_key_count"));
    registerSensor(
        new LambdaStat(
            (ignored, ignored2) -> scatterGatherStats.getTotalRetriesDiscarded(),
            "retry_slower_than_original_count"));
    registerSensor(
        new LambdaStat((ignored, ignored2) -> scatterGatherStats.getTotalRetriesError(), "retry_error_count"));
    registerSensor(
        new LambdaStat(
            (ignored, ignored2) -> scatterGatherStats.getTotalRetriesWinner(),
            "retry_faster_than_original_count"));

    keyNumSensor = registerSensor("key_num", new Avg(), new Max(0));
    /**
     * request_usage.Total is incoming KPS while request_usage.OccurrenceRate is QPS
     */
    requestUsageSensor = registerSensor("request_usage", new Total(), new OccurrenceRate());
    multiGetFallbackSensor = registerSensor("multiget_fallback", new Total(), new OccurrenceRate());

    requestParsingLatencySensor = registerSensor("request_parse_latency", new Avg());
    requestRoutingLatencySensor = registerSensor("request_route_latency", new Avg());

    unAvailableRequestSensor = registerSensor("unavailable_request", new Count());

    delayConstraintAbortedRetryRequest = registerSensor("delay_constraint_aborted_retry_request", new Count());
    slowRouteAbortedRetryRequest = registerSensor("slow_route_aborted_retry_request", new Count());
    retryRouteLimitAbortedRetryRequest = registerSensor("retry_route_limit_aborted_retry_request", new Count());
    noAvailableReplicaAbortedRetryRequest = registerSensor("no_available_replica_aborted_retry_request", new Count());

    readQuotaUsageSensor = registerSensor("read_quota_usage_kps", new Total());

    inFlightRequestSensor = registerSensor("in_flight_request_count", new Min(), new Max(0), new Avg());

    String responseSizeSensorName = "response_size";
    if (isKeyValueProfilingEnabled && storeName.equals(STORE_NAME_FOR_TOTAL_STAT)) {
      String keySizeSensorName = "key_size_in_byte";
      keySizeSensor = registerSensor(
          keySizeSensorName,
          new Avg(),
          new Max(),
          TehutiUtils.getFineGrainedPercentileStat(getName(), getFullMetricName(keySizeSensorName)));
      responseSizeSensor = registerSensor(
          responseSizeSensorName,
          new Avg(),
          new Max(),
          TehutiUtils.getFineGrainedPercentileStat(getName(), getFullMetricName(responseSizeSensorName)));
    } else {
      responseSizeSensor = registerSensor(
          responseSizeSensorName,
          new Avg(),
          new Max(),
          TehutiUtils.getPercentileStat(getName(), getFullMetricName(responseSizeSensorName)));
    }
    currentInFlightRequest = new AtomicInteger();

    allowedRetryRequestSensor = registerSensor("allowed_retry_request_count", new OccurrenceRate());
    disallowedRetryRequestSensor = registerSensor("disallowed_retry_request_count", new OccurrenceRate());
    errorRetryAttemptTriggeredByPendingRequestCheckSensor =
        registerSensor("error_retry_attempt_triggered_by_pending_request_check", new OccurrenceRate());
    retryDelaySensor = registerSensor("retry_delay", new Avg(), new Max());
    metaStoreShadowReadSensor = registerSensor("meta_store_shadow_read", new OccurrenceRate());
  }

  /**
   * We record this at the beginning of request handling, so we don't know the latency yet... All specific
   * types of requests also have their latencies logged at the same time.
   */
  public void recordRequest() {
    requestSensor.record();
    requestSensorOtel.add(1, this.metricDimensions);
    inFlightRequestSensor.record(currentInFlightRequest.incrementAndGet());
    totalInflightRequestSensor.record();
  }

  public void recordHealthyRequest(Double latency) {
    healthySensor.record();
    healthySensorOtel.add(1, this.metricDimensions);
    if (latency != null) {
      healthyRequestLatencySensor.record(latency);
      healthyRequestLatencySensorOtel.record(latency);
    }
  }

  public void recordUnhealthyRequest() {
    unhealthySensor.record();
    unhealthySensorOtel.add(1, this.metricDimensions);
  }

  public void recordUnavailableReplicaStreamingRequest() {
    unavailableReplicaStreamingRequestSensor.record();
  }

  public void recordUnhealthyRequest(double latency) {
    recordUnhealthyRequest();
    unhealthyRequestLatencySensor.record(latency);
    unhealthyRequestLatencySensorOtel.record(latency);
  }

  /**
   * Record read quota usage based on healthy KPS.
   * @param quotaUsage
   */
  public void recordReadQuotaUsage(int quotaUsage) {
    readQuotaUsageSensor.record(quotaUsage);
  }

  public void recordTardyRequest(double latency) {
    tardySensor.record();
    tardySensorOtel.add(1, this.metricDimensions);
    tardyRequestLatencySensor.record(latency);
    tardyRequestLatencySensorOtel.record(latency);
  }

  public void recordThrottledRequest(double latency) {
    recordThrottledRequest();
    throttledRequestLatencySensor.record(latency);
  }

  /**
   * Once we stop reporting throttled requests in {@link com.linkedin.venice.router.api.RouterExceptionAndTrackingUtils},
   * and we only report them in {@link com.linkedin.venice.router.api.VeniceResponseAggregator} then we will always have
   * a latency and we'll be able to remove this overload.
   *
   * TODO: Remove this overload after fixing the above.
   */
  public void recordThrottledRequest() {
    throttleSensor.record();
  }

  public void recordErrorRetryCount() {
    errorRetryCountSensor.record();
  }

  public void recordBadRequest() {
    badRequestSensor.record();
  }

  public void recordBadRequestKeyCount(int keyCount) {
    badRequestKeyCountSensor.record(keyCount);
  }

  public void recordRequestThrottledByRouterCapacity() {
    requestThrottledByRouterCapacitySensor.record();
  }

  public void recordFanoutRequestCount(int count) {
    if (!getRequestType().equals(RequestType.SINGLE_GET)) {
      fanoutRequestCountSensor.record(count);
    }
  }

  public void recordLatency(double latency) {
    latencySensor.record(latency);
    latencySensorOtel.record(latency, metricDimensions);
  }

  public void recordResponseWaitingTime(double waitingTime) {
    routerResponseWaitingTimeSensor.record(waitingTime);
  }

  public void recordRequestSize(double requestSize) {
    requestSizeSensor.record(requestSize);
  }

  public void recordCompressedResponseSize(double compressedResponseSize) {
    compressedResponseSizeSensor.record(compressedResponseSize);
  }

  public void recordResponseSize(double responseSize) {
    responseSizeSensor.record(responseSize);
  }

  public void recordDecompressionTime(double decompressionTime) {
    decompressionTimeSensor.record(decompressionTime);
  }

  public void recordQuota(double quota) {
    quotaSensor.record(quota);
  }

  public void recordFindUnhealthyHostRequest() {
    findUnhealthyHostRequestSensor.record();
  }

  public void recordKeyNum(int keyNum) {
    keyNumSensor.record(keyNum);
  }

  public void recordRequestUsage(int usage) {
    requestUsageSensor.record(usage);
  }

  public void recordMultiGetFallback(int keyCount) {
    multiGetFallbackSensor.record(keyCount);
  }

  public void recordRequestParsingLatency(double latency) {
    requestParsingLatencySensor.record(latency);
  }

  public void recordRequestRoutingLatency(double latency) {
    requestRoutingLatencySensor.record(latency);
  }

  public void recordUnavailableRequest() {
    unAvailableRequestSensor.record();
  }

  public void recordDelayConstraintAbortedRetryRequest() {
    delayConstraintAbortedRetryRequest.record();
  }

  public void recordSlowRouteAbortedRetryRequest() {
    slowRouteAbortedRetryRequest.record();
  }

  public void recordRetryRouteLimitAbortedRetryRequest() {
    retryRouteLimitAbortedRetryRequest.record();
  }

  public void recordNoAvailableReplicaAbortedRetryRequest() {
    noAvailableReplicaAbortedRetryRequest.record();
  }

  public void recordKeySizeInByte(long keySize) {
    if (keySizeSensor != null) {
      keySizeSensor.record(keySize);
    }
  }

  public void recordResponse() {
    /**
     * We already report into the sensor when the request starts, in {@link #recordRequest()}, so at response time
     * there is no need to record into the sensor again. We just want to maintain the bookkeeping.
     */
    currentInFlightRequest.decrementAndGet();
    totalInflightRequestSensor.record(-1);
  }

  public void recordAllowedRetryRequest() {
    allowedRetryRequestSensor.record();
  }

  public void recordDisallowedRetryRequest() {
    disallowedRetryRequestSensor.record();
  }

  public void recordErrorRetryAttemptTriggeredByPendingRequestCheck() {
    errorRetryAttemptTriggeredByPendingRequestCheckSensor.record();
  }

  public void recordRetryDelay(double delay) {
    retryDelaySensor.record(delay);
  }

  public void recordMetaStoreShadowRead() {
    metaStoreShadowReadSensor.record();
  }

  @Override
  protected Sensor registerSensor(String sensorName, MeasurableStat... stats) {
    return super.registerSensor(systemStoreName == null ? sensorName : systemStoreName, null, stats);
  }

  static public boolean hasInFlightRequests() {
    Metric metric = localMetricRepo.getMetric("total_inflight_request_count");
    // max return -infinity when there are no samples. validate only against finite value
    return Double.isFinite(metric.value()) ? metric.value() > 0.0 : false;
  }
}
