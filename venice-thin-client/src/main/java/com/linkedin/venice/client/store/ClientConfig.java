package com.linkedin.venice.client.store;

import com.linkedin.d2.balancer.D2Client;
import com.linkedin.security.ssl.access.control.SSLEngineComponentFactory;
import com.linkedin.venice.client.store.deserialization.BatchGetDeserializer;
import com.linkedin.venice.client.store.deserialization.BatchGetDeserializerType;
import com.linkedin.venice.client.store.deserialization.BlockingDeserializer;
import com.linkedin.venice.serializer.AvroGenericDeserializer;
import io.tehuti.metrics.MetricsRepository;
import java.util.concurrent.Executor;
import org.apache.avro.specific.SpecificRecord;


public class ClientConfig<T extends SpecificRecord> {
  private static final String DEFAULT_D2_SERVICE_NAME = "VeniceRouter";
  private static final String DEFAULT_D2_ZK_BASE_PATH = "/d2";
  private static final String HTTPS = "https";
  public static final int DEFAULT_ZK_TIMEOUT_MS = 5000;

  private String storeName;

  private String veniceURL;

  private Class<T> specificValueClass = null;

  //D2 specific settings
  private boolean isD2Routing = false;
  private String d2ServiceName = DEFAULT_D2_SERVICE_NAME;
  private String d2BasePath = DEFAULT_D2_ZK_BASE_PATH;
  private int d2ZkTimeout = DEFAULT_ZK_TIMEOUT_MS;
  private D2Client d2Client = null;
  private MetricsRepository metricsRepository = null;
  private Executor deserializationExecutor = null;
  private boolean isVsonClient = false;
  private BatchGetDeserializerType batchGetDeserializerType = BatchGetDeserializerType.BLOCKING;
  private AvroGenericDeserializer.IterableImpl multiGetEnvelopeIterableImpl = AvroGenericDeserializer.IterableImpl.BLOCKING;
  private int onDemandDeserializerNumberOfRecordsPerThread = 250;
  private int alwaysOnDeserializerNumberOfThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
  private int alwaysOnDeserializerQueueCapacity = 10000;

  //https specific settings
  private boolean isHttps = false;
  private SSLEngineComponentFactory sslEngineComponentFactory = null;


  public static ClientConfig defaultGenericClientConfig(String storeName) {
    return new ClientConfig(storeName);
  }

  public static ClientConfig defaultVsonGenericClientConfig(String storeName) {
    return new ClientConfig(storeName).setVsonClient(true);
  }

  public static <V extends SpecificRecord> ClientConfig<V> defaultSpecificClientConfig(String storeName,
      Class<V> specificValueClass) {
    return new ClientConfig<V>(storeName)
        .setSpecificValueClass(specificValueClass);
  }

  public static <V extends SpecificRecord> ClientConfig<V> cloneConfig(ClientConfig<V> config) {
    ClientConfig<V> newConfig = new ClientConfig<>();
    newConfig.setSpecificValueClass(config.getSpecificValueClass())
             .setStoreName(config.getStoreName())
             .setVeniceURL(config.getVeniceURL())
             .setD2Routing(config.isD2Routing())
             .setD2ServiceName(config.getD2ServiceName())
             .setD2BasePath(config.getD2BasePath())
             .setD2ZkTimeout(config.getD2ZkTimeout())
             .setD2Client(config.getD2Client())
             .setHttps(config.isHttps)
             .setSslEngineComponentFactory(config.getSslEngineComponentFactory())
             .setMetricsRepository(config.getMetricsRepository())
             .setVsonClient(config.isVsonClient)
             .setBatchGetDeserializerType(config.batchGetDeserializerType)
             .setMultiGetEnvelopeIterableImpl(config.multiGetEnvelopeIterableImpl)
             .setOnDemandDeserializerNumberOfRecordsPerThread(config.onDemandDeserializerNumberOfRecordsPerThread)
             .setAlwaysOnDeserializerNumberOfThreads(config.alwaysOnDeserializerNumberOfThreads)
             .setAlwaysOnDeserializerQueueCapacity(config.alwaysOnDeserializerQueueCapacity);

    return newConfig;
  }

  private ClientConfig() {}

  private ClientConfig(String storeName) {
    this.storeName = storeName;
  }

  public String getStoreName() {
    return storeName;
  }

  public ClientConfig setStoreName(String storeName) {
    this.storeName = storeName;
    return this;
  }

  public String getVeniceURL() {
    return veniceURL;
  }

  /**
   * @param veniceURL If using D2, this should be D2 ZK address.
   *                     Otherwise, it should be router address.
   */
  public ClientConfig<T> setVeniceURL(String veniceURL) {
    if (veniceURL != null && veniceURL.startsWith(HTTPS)) {
      setHttps(true);
    } else {
      setHttps(false);
    }

    this.veniceURL = veniceURL;
    return this;
  }

  public Class<T> getSpecificValueClass() {
    return specificValueClass;
  }

  public ClientConfig<T> setSpecificValueClass(Class<T> specificValueClass) {
    this.specificValueClass = specificValueClass;
    return this;
  }

  public boolean isSpecificClient() {
    return specificValueClass != null;
  }

  public boolean isD2Routing() {
    return isD2Routing;
  }

  //This is identified automatically when a D2 service name is passed in
  private ClientConfig<T> setD2Routing(boolean isD2Routing) {
    this.isD2Routing = isD2Routing;
    return this;
  }

  public String getD2ServiceName() {
    return d2ServiceName;
  }

  public ClientConfig<T> setD2ServiceName(String d2ServiceName) {
    if (d2ServiceName != null) {
      setD2Routing(true);
      this.d2ServiceName = d2ServiceName;
    } else {
      setD2Routing(false);
    }

    return this;
  }

  public String getD2BasePath() {
    return d2BasePath;
  }

  public ClientConfig<T> setD2BasePath(String d2BasePath) {
    this.d2BasePath = d2BasePath;
    return this;
  }

  public int getD2ZkTimeout() {
    return d2ZkTimeout;
  }

  public ClientConfig<T> setD2ZkTimeout(int d2ZkTimeout) {
    this.d2ZkTimeout = d2ZkTimeout;
    return this;
  }

  public D2Client getD2Client() {
    return d2Client;
  }

  public ClientConfig<T> setD2Client(D2Client d2Client) {
    this.d2Client = d2Client;
    return this;
  }

  public boolean isHttps() {
    return isHttps;
  }

  //this is identified automatically when a URL is passed in
  private ClientConfig<T> setHttps(boolean isHttps) {
    this.isHttps = isHttps;
    return this;
  }

  public SSLEngineComponentFactory getSslEngineComponentFactory() {
    return sslEngineComponentFactory;
  }

  public ClientConfig<T> setSslEngineComponentFactory(SSLEngineComponentFactory sslEngineComponentFactory) {
    this.sslEngineComponentFactory = sslEngineComponentFactory;
    return this;
  }


  public MetricsRepository getMetricsRepository() {
    return metricsRepository;
  }
  public ClientConfig<T> setMetricsRepository(MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
    return this;
  }

  public Executor getDeserializationExecutor() {
    return deserializationExecutor;
  }

  /**
   * Provide an arbitrary executor to execute client requests in,
   * rather than letting the client use its own internally-generated executor.
   * If null, or unset, the client will use {@link java.util.concurrent.Executors#newFixedThreadPool(int)}
   * with a thread limit equal to half the CPU cores.
   */
  public ClientConfig<T> setDeserializationExecutor(Executor deserializationExecutor) {
    this.deserializationExecutor = deserializationExecutor;
    return this;
  }

  public boolean isVsonClient() {
    return isVsonClient;
  }

  public ClientConfig<T> setVsonClient(boolean isVonClient) {
    this.isVsonClient = isVonClient;
    return this;
  }

  public BatchGetDeserializerType getBatchGetDeserializerType() {
    return batchGetDeserializerType;
  }

  public BatchGetDeserializer getBatchGetDeserializer(Executor executor) {
    return batchGetDeserializerType.get(executor, this);
  }

  public ClientConfig<T> setBatchGetDeserializerType(BatchGetDeserializerType batchGetDeserializerType) {
    this.batchGetDeserializerType = batchGetDeserializerType;
    return this;
  }

  public AvroGenericDeserializer.IterableImpl getMultiGetEnvelopeIterableImpl() {
    return multiGetEnvelopeIterableImpl;
  }

  public ClientConfig<T> setMultiGetEnvelopeIterableImpl(AvroGenericDeserializer.IterableImpl multiGetEnvelopeIterableImpl) {
    this.multiGetEnvelopeIterableImpl = multiGetEnvelopeIterableImpl;
    return this;
  }

  public int getOnDemandDeserializerNumberOfRecordsPerThread() {
    return onDemandDeserializerNumberOfRecordsPerThread;
  }

  public ClientConfig<T> setOnDemandDeserializerNumberOfRecordsPerThread(int onDemandDeserializerNumberOfRecordsPerThread) {
    this.onDemandDeserializerNumberOfRecordsPerThread = onDemandDeserializerNumberOfRecordsPerThread;
    return this;
  }

  public int getAlwaysOnDeserializerNumberOfThreads() {
    return alwaysOnDeserializerNumberOfThreads;
  }

  public ClientConfig<T> setAlwaysOnDeserializerNumberOfThreads(int alwaysOnDeserializerNumberOfThreads) {
    this.alwaysOnDeserializerNumberOfThreads = alwaysOnDeserializerNumberOfThreads;
    return this;
  }

  public int getAlwaysOnDeserializerQueueCapacity() {
    return alwaysOnDeserializerQueueCapacity;
  }

  public ClientConfig<T> setAlwaysOnDeserializerQueueCapacity(int alwaysOnDeserializerQueueCapacity) {
    this.alwaysOnDeserializerQueueCapacity = alwaysOnDeserializerQueueCapacity;
    return this;
  }
}
