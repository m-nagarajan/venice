/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.linkedin.venice.controller.kafka.protocol.admin;

@SuppressWarnings("all")
public class AdminOperation extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"AdminOperation\",\"namespace\":\"com.linkedin.venice.controller.kafka.protocol.admin\",\"fields\":[{\"name\":\"operationType\",\"type\":\"int\",\"doc\":\"0 => StoreCreation, 1 => ValueSchemaCreation, 2 => PauseStore, 3 => ResumeStore, 4 => KillOfflinePushJob, 5 => DisableStoreRead, 6 => EnableStoreRead, 7=> DeleteAllVersions, 8=> SetStoreOwner, 9=> SetStorePartitionCount, 10=> SetStoreCurrentVersion, 11=> UpdateStore, 12=> DeleteStore, 13=> DeleteOldVersion, 14=> MigrateStore, 15=> AbortMigration, 16=>AddVersion, 17=> DerivedSchemaCreation, 18=>SupersetSchemaCreation, 19=>EnableNativeReplicationForCluster, 20=>MetadataSchemaCreation\"},{\"name\":\"executionId\",\"type\":\"long\",\"doc\":\"ID of a command execution which is used to query the status of this command.\",\"default\":0},{\"name\":\"payloadUnion\",\"type\":[{\"type\":\"record\",\"name\":\"StoreCreation\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"owner\",\"type\":\"string\"},{\"name\":\"keySchema\",\"type\":{\"type\":\"record\",\"name\":\"SchemaMeta\",\"fields\":[{\"name\":\"schemaType\",\"type\":\"int\",\"doc\":\"0 => Avro-1.4, and we can add more if necessary\"},{\"name\":\"definition\",\"type\":\"string\"}]}},{\"name\":\"valueSchema\",\"type\":\"SchemaMeta\"}]},{\"type\":\"record\",\"name\":\"ValueSchemaCreation\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"schema\",\"type\":\"SchemaMeta\"},{\"name\":\"schemaId\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"PauseStore\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"ResumeStore\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"KillOfflinePushJob\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"kafkaTopic\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"DisableStoreRead\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"EnableStoreRead\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"DeleteAllVersions\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"SetStoreOwner\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"owner\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"SetStorePartitionCount\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"partitionNum\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"SetStoreCurrentVersion\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"currentVersion\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"UpdateStore\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"owner\",\"type\":\"string\"},{\"name\":\"partitionNum\",\"type\":\"int\"},{\"name\":\"currentVersion\",\"type\":\"int\"},{\"name\":\"enableReads\",\"type\":\"boolean\"},{\"name\":\"enableWrites\",\"type\":\"boolean\"},{\"name\":\"storageQuotaInByte\",\"type\":\"long\",\"default\":21474836480},{\"name\":\"readQuotaInCU\",\"type\":\"long\",\"default\":1800},{\"name\":\"hybridStoreConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"HybridStoreConfigRecord\",\"fields\":[{\"name\":\"rewindTimeInSeconds\",\"type\":\"long\"},{\"name\":\"offsetLagThresholdToGoOnline\",\"type\":\"long\"},{\"name\":\"producerTimestampLagThresholdToGoOnlineInSeconds\",\"type\":\"long\",\"default\":-1},{\"name\":\"dataReplicationPolicy\",\"type\":\"int\",\"doc\":\"Real-time Samza job data replication policy. Using int because Avro Enums are not evolvable 0 => NON_AGGREGATE, 1 => AGGREGATE, 2 => ACTIVE_ACTIVE\",\"default\":0},{\"name\":\"bufferReplayPolicy\",\"type\":\"int\",\"doc\":\"Policy that will be used during buffer replay. rewindTimeInSeconds defines the delta. 0 => REWIND_FROM_EOP (replay from 'EOP - rewindTimeInSeconds'), 1 => REWIND_FROM_SOP (replay from 'SOP - rewindTimeInSeconds')\",\"default\":0}]}],\"default\":null},{\"name\":\"accessControlled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"compressionStrategy\",\"type\":\"int\",\"doc\":\"Using int because Avro Enums are not evolvable\",\"default\":0},{\"name\":\"chunkingEnabled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"singleGetRouterCacheEnabled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"batchGetRouterCacheEnabled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"batchGetLimit\",\"type\":\"int\",\"doc\":\"The max key number allowed in batch get request, and Venice will use cluster-level config if the limit (not positive) is not valid\",\"default\":-1},{\"name\":\"numVersionsToPreserve\",\"type\":\"int\",\"doc\":\"The max number of versions the store should preserve. Venice will use cluster-level config if the number is 0 here.\",\"default\":0},{\"name\":\"incrementalPushEnabled\",\"type\":\"boolean\",\"doc\":\"a flag to see if the store supports incremental push or not\",\"default\":false},{\"name\":\"isMigrating\",\"type\":\"boolean\",\"doc\":\"Whether or not the store is in the process of migration\",\"default\":false},{\"name\":\"writeComputationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether write-path computation feature is enabled for this store\",\"default\":false},{\"name\":\"readComputationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether read-path computation feature is enabled for this store\",\"default\":false},{\"name\":\"bootstrapToOnlineTimeoutInHours\",\"type\":\"int\",\"doc\":\"Maximum number of hours allowed for the store to transition from bootstrap to online state\",\"default\":24},{\"name\":\"leaderFollowerModelEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not to use leader follower state transition model for upcoming version\",\"default\":false},{\"name\":\"backupStrategy\",\"type\":\"int\",\"doc\":\"Strategies to store backup versions.\",\"default\":0},{\"name\":\"clientDecompressionEnabled\",\"type\":\"boolean\",\"default\":true},{\"name\":\"schemaAutoRegisterFromPushJobEnabled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"hybridStoreOverheadBypass\",\"type\":\"boolean\",\"default\":false},{\"name\":\"hybridStoreDiskQuotaEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not to enable disk storage quota for a hybrid store\",\"default\":false},{\"name\":\"ETLStoreConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"ETLStoreConfigRecord\",\"fields\":[{\"name\":\"etledUserProxyAccount\",\"type\":[\"null\",\"string\"]},{\"name\":\"regularVersionETLEnabled\",\"type\":\"boolean\"},{\"name\":\"futureVersionETLEnabled\",\"type\":\"boolean\"}]}],\"default\":null},{\"name\":\"partitionerConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"PartitionerConfigRecord\",\"fields\":[{\"name\":\"partitionerClass\",\"type\":\"string\"},{\"name\":\"partitionerParams\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"amplificationFactor\",\"type\":\"int\"}]}],\"default\":null},{\"name\":\"nativeReplicationEnabled\",\"type\":\"boolean\",\"default\":false},{\"name\":\"pushStreamSourceAddress\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"largestUsedVersionNumber\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"incrementalPushPolicy\",\"type\":\"int\",\"doc\":\"Incremental Push Policy to reconcile with real time pushes. Using int because Avro Enums are not evolvable 0 => PUSH_TO_VERSION_TOPIC, 1 => INCREMENTAL_PUSH_SAME_AS_REAL_TIME\",\"default\":0},{\"name\":\"backupVersionRetentionMs\",\"type\":\"long\",\"doc\":\"Backup version retention time after a new version is promoted to the current version, if not specified, Venice will use the configured retention as the default policy\",\"default\":-1},{\"name\":\"replicationFactor\",\"type\":\"int\",\"doc\":\"number of replica each store version will have\",\"default\":3},{\"name\":\"migrationDuplicateStore\",\"type\":\"boolean\",\"doc\":\"Whether or not the store is a duplicate store in the process of migration\",\"default\":false},{\"name\":\"nativeReplicationSourceFabric\",\"type\":[\"null\",\"string\"],\"doc\":\"The source fabric to be used when the store is running in Native Replication mode.\",\"default\":null},{\"name\":\"activeActiveReplicationEnabled\",\"type\":\"boolean\",\"doc\":\"A command option to enable/disable Active/Active replication feature for a store\",\"default\":false},{\"name\":\"updatedConfigsList\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"doc\":\"The list that contains all updated configs by the UpdateStore command. Most of the fields in UpdateStore are not optional, and changing those fields to Optional (Union) is not a backward compatible change, so we have to add an addition array field to record all updated configs in parent controller.\",\"default\":[]},{\"name\":\"replicateAllConfigs\",\"type\":\"boolean\",\"doc\":\"A flag to indicate whether all store configs in parent cluster will be replicated to child clusters; true by default, so that existing UpdateStore messages in Admin topic will behave the same as before.\",\"default\":true},{\"name\":\"regionsFilter\",\"type\":[\"null\",\"string\"],\"doc\":\"A list of regions that will be impacted by the UpdateStore command\",\"default\":null}]},{\"type\":\"record\",\"name\":\"DeleteStore\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"largestUsedVersionNumber\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"DeleteOldVersion\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"versionNum\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"MigrateStore\",\"fields\":[{\"name\":\"srcClusterName\",\"type\":\"string\"},{\"name\":\"destClusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"AbortMigration\",\"fields\":[{\"name\":\"srcClusterName\",\"type\":\"string\"},{\"name\":\"destClusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"AddVersion\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"pushJobId\",\"type\":\"string\"},{\"name\":\"versionNum\",\"type\":\"int\"},{\"name\":\"numberOfPartitions\",\"type\":\"int\"},{\"name\":\"pushType\",\"type\":\"int\",\"doc\":\"The push type of the new version, 0 => BATCH, 1 => STREAM_REPROCESSING. Previous add version messages will default to BATCH and this is a safe because they were created when BATCH was the only version type\",\"default\":0},{\"name\":\"pushStreamSourceAddress\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"rewindTimeInSecondsOverride\",\"type\":\"long\",\"doc\":\"The overridable rewind time config for this specific version of a hybrid store, and if it is not specified, the new version will use the store-level rewind time config\",\"default\":-1},{\"name\":\"timestampMetadataVersionId\",\"type\":\"int\",\"doc\":\"The A/A metadata schema version ID that will be used to deserialize metadataPayload.\",\"default\":-1}]},{\"type\":\"record\",\"name\":\"DerivedSchemaCreation\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"schema\",\"type\":\"SchemaMeta\"},{\"name\":\"valueSchemaId\",\"type\":\"int\"},{\"name\":\"derivedSchemaId\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"SupersetSchemaCreation\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"valueSchema\",\"type\":\"SchemaMeta\"},{\"name\":\"valueSchemaId\",\"type\":\"int\"},{\"name\":\"supersetSchema\",\"type\":\"SchemaMeta\"},{\"name\":\"supersetSchemaId\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"ConfigureNativeReplicationForCluster\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeType\",\"type\":\"string\"},{\"name\":\"enabled\",\"type\":\"boolean\"},{\"name\":\"nativeReplicationSourceRegion\",\"type\":[\"null\",\"string\"],\"doc\":\"The source region to be used when the store is running in Native Replication mode.\",\"default\":null},{\"name\":\"regionsFilter\",\"type\":[\"null\",\"string\"],\"default\":null}]},{\"type\":\"record\",\"name\":\"MetadataSchemaCreation\",\"fields\":[{\"name\":\"clusterName\",\"type\":\"string\"},{\"name\":\"storeName\",\"type\":\"string\"},{\"name\":\"valueSchemaId\",\"type\":\"int\"},{\"name\":\"metadataSchema\",\"type\":\"SchemaMeta\"},{\"name\":\"timestampMetadataVersionId\",\"type\":\"int\"}]}],\"doc\":\"This contains the main payload of the admin operation\"}]}");
  /** 0 => StoreCreation, 1 => ValueSchemaCreation, 2 => PauseStore, 3 => ResumeStore, 4 => KillOfflinePushJob, 5 => DisableStoreRead, 6 => EnableStoreRead, 7=> DeleteAllVersions, 8=> SetStoreOwner, 9=> SetStorePartitionCount, 10=> SetStoreCurrentVersion, 11=> UpdateStore, 12=> DeleteStore, 13=> DeleteOldVersion, 14=> MigrateStore, 15=> AbortMigration, 16=>AddVersion, 17=> DerivedSchemaCreation, 18=>SupersetSchemaCreation, 19=>EnableNativeReplicationForCluster, 20=>MetadataSchemaCreation */
  public int operationType;
  /** ID of a command execution which is used to query the status of this command. */
  public long executionId;
  /** This contains the main payload of the admin operation */
  public java.lang.Object payloadUnion;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return operationType;
    case 1: return executionId;
    case 2: return payloadUnion;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: operationType = (java.lang.Integer)value$; break;
    case 1: executionId = (java.lang.Long)value$; break;
    case 2: payloadUnion = (java.lang.Object)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}
