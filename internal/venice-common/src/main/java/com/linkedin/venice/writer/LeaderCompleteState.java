package com.linkedin.venice.writer;

import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.utils.EnumUtils;
import com.linkedin.venice.utils.VeniceEnumValue;


/**
 * Completion state of the leader partition.
 */
public enum LeaderCompleteState implements VeniceEnumValue {
  /**
   * Leader partition is not completed yet
   */
  LEADER_NOT_COMPLETED(0),
  /**
   * Leader partition is completed
   */
  LEADER_COMPLETED(1),
  /**
   * Heartbeat is not received yet: Hybrid standby replica will wait for the first heartbeat
   * to arrive before marking itself completed.
   */
  LEADER_COMPLETE_STATE_UNKNOWN(2);

  /**
   * - Default state of the replicas to start with
   * - Heartbeat is received without leader state header: standby will not wait for the leader to complete.
   * This will be a temporary state until the supported version is deployed to all servers
   */
  // LEADER_COMPLETE_STATE_HEADER_NOT_SUPPORTED(3);

  private final int value;
  private static final LeaderCompleteState[] TYPES_ARRAY = EnumUtils.getEnumValuesArray(LeaderCompleteState.class);

  LeaderCompleteState(int value) {
    this.value = value;
  }

  public static LeaderCompleteState getLeaderCompleteState(boolean isLeaderCompleted) {
    if (isLeaderCompleted) {
      return LEADER_COMPLETED;
    } else {
      return LEADER_NOT_COMPLETED;
    }
  }

  public static LeaderCompleteState valueOf(int value) {
    try {
      return TYPES_ARRAY[value];
    } catch (IndexOutOfBoundsException e) {
      throw new VeniceException("Invalid LeaderCompleteState: " + value);
    }
  }

  @Override
  public int getValue() {
    return value;
  }
}
