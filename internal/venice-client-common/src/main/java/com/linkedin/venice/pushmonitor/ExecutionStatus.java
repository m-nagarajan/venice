package com.linkedin.venice.pushmonitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Status of executing off-line push. The status will be used by both Job and Task.
 *
 * Adding new status will break the Controller to Storage Node communication.
 * This is used as part of StoreStatusMessage. When adding states,
 * backward compat needs to be fixed, after things are in production at least.
 *
 * Whenever a new status is introduced, we should follow the correct deployment
 * order:
 * The consumers of the execution status should be deployed with the latest code
 * before anyone reports the new status; the consumers include controllers, routers,
 * servers at this moment; in future, there could also be Samza users that depend
 * on ExecutionStatus.
 *
 * TODO: Break this up in JobExecutionStatus and TaskExecutionStatus. It's pretty confusing to mix them ): ...
 */
public enum ExecutionStatus {
  /** Job doesn't yet exist */
  NOT_CREATED(true, false, false, false, 0),

  /**
   * Legacy status.
   *
   * Now only used as the initial value for an {@link ExecutionStatus} in VeniceParentHelixAdmin, but should
   * never actually be returned to the push job, under normal circumstances...
   */
  NEW(true, true, false, false, 1),

  /** Job/Task is started and start consuming data from Kafka */
  STARTED(true, true, false, false, 2),

  /** Task is processing data. */
  PROGRESS(false, true, false, false, 3),

  /** Tasks belonging to a Hybrid Store emits this instead of {@link ExecutionStatus#COMPLETED} when it consumes a EOP message */
  END_OF_PUSH_RECEIVED(true, true, true, false, 4),

  /** Tasks belonging to a Hybrid Store emits this instead when it consumes a SOBR message */
  @Deprecated
  START_OF_BUFFER_REPLAY_RECEIVED(false, true, true, false, 5),

  /** Tasks belonging to a Hybrid Store emits this instead when it consumes a TS message */
  TOPIC_SWITCH_RECEIVED(false, true, true, false, 6),

  /** An incremental push job/task is started*/
  START_OF_INCREMENTAL_PUSH_RECEIVED(true, true, false, false, 7),

  /** An incremental push job/task is completed*/
  END_OF_INCREMENTAL_PUSH_RECEIVED(true, true, false, true, 8),

  /* Task is dropped by the storage node */
  @Deprecated
  DROPPED(false, true, false, false, 9),

  /**
   * For task, data is read and put into storage engine.
   * For Job, all of tasks are completed.
   */
  COMPLETED(true, true, false, true, 10),

  /** a non-fatal error task meets when processing the data. Often happens after EOP is received. **/
  @Deprecated
  WARNING(false, true, false, true, 11),

  /** Job/task met error when processing the data. */
  ERROR(true, true, false, true, 12),

  CATCH_UP_BASE_TOPIC_OFFSET_LAG(false, true, true, false, 13),

  /**
   * Job is terminated and be removed from repository. Should be archived to historic data storage.
   * Only be used for Job
   * TODO: remove ARCHIVED as it's not been used anymore
   */
  @Deprecated
  ARCHIVED(true, false, false, true, 14),

  /** Job status is unknown when checking, and it could be caused by network issue */
  UNKNOWN(true, false, false, false, 15),

  /** Job/Task is created but ingestion haven't started yet */
  NOT_STARTED(false, true, false, false, 16),

  DATA_RECOVERY_COMPLETED(false, true, false, false, 17),

  // DVC specific ERRORS: rootStatus is ERROR
  DVC_INGESTION_ERROR_DISK_FULL(true, false, true, true, ERROR, 18),

  DVC_INGESTION_ERROR_MEMORY_LIMIT_REACHED(true, false, true, true, ERROR, 19),

  DVC_INGESTION_ERROR_TOO_MANY_DEAD_INSTANCES(true, false, true, true, ERROR, 20),

  DVC_INGESTION_ERROR_OTHER(true, false, true, true, ERROR, 21);

  final boolean isJobStatus;
  final boolean isTaskStatus;
  final boolean isUsedByDaVinciClientOnly;
  final boolean isTerminal;
  final ExecutionStatus rootStatus;
  final int value;

  ExecutionStatus(
      boolean isJobStatus,
      boolean isTaskStatus,
      boolean isUsedByDaVinciClientOnly,
      boolean isTerminal,
      int value) {
    this.isJobStatus = isJobStatus;
    this.isTaskStatus = isTaskStatus;
    this.isUsedByDaVinciClientOnly = isUsedByDaVinciClientOnly;
    this.isTerminal = isTerminal;
    this.value = value;
    this.rootStatus = this;
  }

  ExecutionStatus(
      boolean isJobStatus,
      boolean isTaskStatus,
      boolean isUsedByDaVinciClientOnly,
      boolean isTerminal,
      ExecutionStatus rootStatus,
      int value) {
    this.isJobStatus = isJobStatus;
    this.isTaskStatus = isTaskStatus;
    this.isUsedByDaVinciClientOnly = isUsedByDaVinciClientOnly;
    this.isTerminal = isTerminal;
    this.rootStatus = rootStatus;
    this.value = value;
  }

  /**
   * Some of the statuses are like watermark. These statuses are used in {@link PushMonitor} and
   * {@link com.linkedin.venice.router.api.VeniceVersionFinder} to determine whether a job is finished
   * and whether a host is ready to serve read requests.
   */
  public static boolean isDeterminedStatus(ExecutionStatus status) {
    ExecutionStatus rootStatus = status.getRootStatus();
    return rootStatus == STARTED || rootStatus == COMPLETED || rootStatus == ERROR || rootStatus == DROPPED
        || rootStatus == END_OF_PUSH_RECEIVED;
  }

  public boolean isJobStatus() {
    return this.isJobStatus;
  }

  public boolean isTaskStatus() {
    return this.isTaskStatus;
  }

  public boolean isUsedByDaVinciClientOnly() {
    return this.isUsedByDaVinciClientOnly;
  }

  /**
   * @return true for {@link #COMPLETED}, {@link #ERROR} and {@link #ARCHIVED}, false otherwise.
   */
  public boolean isTerminal() {
    return this.isTerminal;
  }

  public static boolean isIncrementalPushStatus(ExecutionStatus status) {
    return status == START_OF_INCREMENTAL_PUSH_RECEIVED || status == END_OF_INCREMENTAL_PUSH_RECEIVED;
  }

  public static boolean isIncrementalPushStatus(int statusVal) {
    return statusVal == START_OF_INCREMENTAL_PUSH_RECEIVED.getValue()
        || statusVal == END_OF_INCREMENTAL_PUSH_RECEIVED.getValue();
  }

  public int getValue() {
    return value;
  }

  /**
   * Get ExecutionStatus from integer ordinal value in avro.
   */
  private static final Map<Integer, ExecutionStatus> idMapping = new HashMap<>();
  static {
    Arrays.stream(values()).forEach(s -> idMapping.put(s.value, s));
  }

  public static ExecutionStatus fromInt(int v) {
    ExecutionStatus status = idMapping.get(v);
    if (status == null) {
      return ExecutionStatus.UNKNOWN;
    }
    return status;
  }

  public ExecutionStatus getRootStatus() {
    return this.rootStatus;
  }

  public boolean isDVCIngestionError() {
    return (this.getRootStatus() == ERROR && this.isUsedByDaVinciClientOnly());
  }

  public boolean isError() {
    return (this.getRootStatus() == ERROR);
  }

  public static boolean isError(String errorString) {
    try {
      return (ExecutionStatus.valueOf(errorString.toUpperCase()).getRootStatus() == ERROR);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
