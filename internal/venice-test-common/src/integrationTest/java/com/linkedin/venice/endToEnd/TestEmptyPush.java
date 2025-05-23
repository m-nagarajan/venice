package com.linkedin.venice.endToEnd;

import static com.linkedin.venice.ConfigKeys.KAFKA_BOOTSTRAP_SERVERS;
import static com.linkedin.venice.pubsub.PubSubConstants.PUBSUB_OPERATION_TIMEOUT_MS_DEFAULT_VALUE;
import static com.linkedin.venice.utils.IntegrationTestPushUtils.defaultVPJProps;
import static com.linkedin.venice.utils.IntegrationTestPushUtils.runVPJ;
import static com.linkedin.venice.utils.IntegrationTestPushUtils.sendStreamingRecord;
import static com.linkedin.venice.utils.TestWriteUtils.STRING_SCHEMA;
import static com.linkedin.venice.utils.TestWriteUtils.USER_SCHEMA;
import static com.linkedin.venice.utils.TestWriteUtils.getTempDataDirectory;
import static com.linkedin.venice.utils.TestWriteUtils.writeEmptyAvroFile;
import static com.linkedin.venice.vpj.VenicePushJobConstants.COMPRESSION_METRIC_COLLECTION_ENABLED;
import static com.linkedin.venice.vpj.VenicePushJobConstants.SEND_CONTROL_MESSAGES_DIRECTLY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.linkedin.venice.client.store.AvroGenericStoreClient;
import com.linkedin.venice.client.store.ClientConfig;
import com.linkedin.venice.client.store.ClientFactory;
import com.linkedin.venice.compression.CompressionStrategy;
import com.linkedin.venice.controllerapi.ControllerClient;
import com.linkedin.venice.controllerapi.UpdateStoreQueryParams;
import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.integration.utils.ServiceFactory;
import com.linkedin.venice.integration.utils.VeniceClusterCreateOptions;
import com.linkedin.venice.integration.utils.VeniceClusterWrapper;
import com.linkedin.venice.meta.Store;
import com.linkedin.venice.meta.Version;
import com.linkedin.venice.pubsub.api.PubSubTopic;
import com.linkedin.venice.pubsub.manager.TopicManager;
import com.linkedin.venice.utils.DataProviderUtils;
import com.linkedin.venice.utils.DictionaryUtils;
import com.linkedin.venice.utils.IntegrationTestPushUtils;
import com.linkedin.venice.utils.TestUtils;
import com.linkedin.venice.utils.Time;
import com.linkedin.venice.utils.Utils;
import com.linkedin.venice.utils.VeniceProperties;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.samza.system.SystemProducer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class TestEmptyPush {
  private VeniceClusterWrapper venice;

  @BeforeClass(alwaysRun = true)
  public void setUp() {
    VeniceClusterCreateOptions options = new VeniceClusterCreateOptions.Builder().numberOfControllers(1)
        .numberOfRouters(1)
        .numberOfServers(2)
        .replicationFactor(2)
        .sslToKafka(false)
        .sslToStorageNodes(false)
        .build();
    venice = ServiceFactory.getVeniceCluster(options);
  }

  @AfterClass(alwaysRun = true)
  public void tearDown() {
    Utils.closeQuietlyWithErrorLogged(venice);
  }

  private ByteBuffer getDictFromTopic(String topic, Properties properties) {
    String kafkaUrl = venice.getPubSubBrokerWrapper().getAddress();

    Properties props = (Properties) properties.clone();
    props.setProperty(KAFKA_BOOTSTRAP_SERVERS, kafkaUrl);
    return DictionaryUtils.readDictionaryFromKafka(topic, new VeniceProperties(props));
  }

  @Test(timeOut = 30 * Time.MS_PER_SECOND, dataProvider = "True-and-False", dataProviderClass = DataProviderUtils.class)
  public void testEmptyPushWithZstdWithDictCompression(boolean sendControlMessageDirectly) throws IOException {
    String storeName = Utils.getUniqueString("test_empty_push_store");
    try (ControllerClient controllerClient =
        new ControllerClient(venice.getClusterName(), venice.getAllControllersURLs())) {
      controllerClient.createNewStore(storeName, "owner", STRING_SCHEMA.toString(), STRING_SCHEMA.toString());
      controllerClient.updateStore(
          storeName,
          new UpdateStoreQueryParams().setStorageQuotaInByte(Store.UNLIMITED_STORAGE_QUOTA)
              .setCompressionStrategy(CompressionStrategy.ZSTD_WITH_DICT));
      File inputDir = getTempDataDirectory();
      String inputDirPath = "file://" + inputDir.getAbsolutePath();
      writeEmptyAvroFile(inputDir, USER_SCHEMA);

      Properties vpjProperties = defaultVPJProps(venice, inputDirPath, storeName);
      vpjProperties.setProperty(SEND_CONTROL_MESSAGES_DIRECTLY, Boolean.toString(sendControlMessageDirectly));
      vpjProperties.setProperty(COMPRESSION_METRIC_COLLECTION_ENABLED, Boolean.toString(true));
      runVPJ(vpjProperties, 1, controllerClient);
      ByteBuffer dict = getDictFromTopic(Version.composeKafkaTopic(storeName, 1), vpjProperties);
      assertNotNull(dict, "Dict shouldn't be null for the empty push with CompressionStrategy as ZSTD_WITH_DICT");
    }
  }

  @Test(timeOut = 120 * Time.MS_PER_SECOND)
  public void testEmptyPushByChangingCompressionStrategyForHybridStore() throws IOException {
    String storeName = Utils.getUniqueString("test_empty_push_store");
    try (
        ControllerClient controllerClient =
            new ControllerClient(venice.getClusterName(), venice.getAllControllersURLs());
        TopicManager topicManager =
            IntegrationTestPushUtils
                .getTopicManagerRepo(
                    PUBSUB_OPERATION_TIMEOUT_MS_DEFAULT_VALUE,
                    100,
                    0L,
                    venice.getPubSubBrokerWrapper(),
                    venice.getPubSubTopicRepository())
                .getTopicManager(venice.getPubSubBrokerWrapper().getAddress())) {
      controllerClient.createNewStore(storeName, "owner", STRING_SCHEMA.toString(), STRING_SCHEMA.toString());
      TestUtils.assertCommand(controllerClient.getStore(storeName));
      controllerClient.updateStore(
          storeName,
          new UpdateStoreQueryParams().setStorageQuotaInByte(Store.UNLIMITED_STORAGE_QUOTA)
              .setHybridRewindSeconds(60)
              .setHybridOffsetLagThreshold(10)
              .setCompressionStrategy(CompressionStrategy.ZSTD_WITH_DICT));

      File inputDir = getTempDataDirectory();
      String inputDirPath = "file://" + inputDir.getAbsolutePath();
      writeEmptyAvroFile(inputDir, USER_SCHEMA);

      // First empty push with dict compression enabled.
      Properties vpjProperties = defaultVPJProps(venice, inputDirPath, storeName);
      runVPJ(vpjProperties, 1, controllerClient);
      ByteBuffer dictForVersion1 = getDictFromTopic(Version.composeKafkaTopic(storeName, 1), vpjProperties);
      assertNotNull(
          dictForVersion1,
          "Dict shouldn't be null for the empty push to a hybrid store without any records in RT");

      String realTimeTopic = Utils.getRealTimeTopicName(controllerClient.getStore(storeName).getStore());
      PubSubTopic storeRealTimeTopic = venice.getPubSubTopicRepository().getTopic(realTimeTopic);
      assertTrue(topicManager.containsTopicAndAllPartitionsAreOnline(storeRealTimeTopic));
      // One time refresh of router metadata.
      venice.refreshAllRouterMetaData();
      // Start writing some real-time records
      SystemProducer veniceProducer =
          IntegrationTestPushUtils.getSamzaProducer(venice, storeName, Version.PushType.STREAM);

      String keyPrefix = "test_key_";
      String valuePrefix = "test_value_";

      for (int i = 1; i <= 1000; i++) {
        sendStreamingRecord(veniceProducer, storeName, keyPrefix + i, valuePrefix + i);
      }

      Runnable dataValidator = () -> {
        try (AvroGenericStoreClient client = ClientFactory.getAndStartGenericAvroClient(
            ClientConfig.defaultGenericClientConfig(storeName).setVeniceURL(venice.getRandomRouterURL()))) {
          TestUtils.waitForNonDeterministicAssertion(30, TimeUnit.SECONDS, () -> {
            try {
              for (int i = 1; i <= 1000; ++i) {
                String key = keyPrefix + i;
                Object value = client.get(key).get();
                assertNotNull(value, "value for key: " + key + " shouldn't be null");
                assertEquals(value.toString(), valuePrefix + i);
              }
            } catch (Exception e) {
              throw new VeniceException(e);
            }
          });
        }
      };
      dataValidator.run();

      // Enable Gzip compression
      controllerClient
          .updateStore(storeName, new UpdateStoreQueryParams().setCompressionStrategy(CompressionStrategy.GZIP));
      // 2nd empty push with Gzip enabled
      runVPJ(vpjProperties, 2, controllerClient);
      ByteBuffer dictForVersion2 = getDictFromTopic(Version.composeKafkaTopic(storeName, 2), vpjProperties);
      assertNull(dictForVersion2, "Dict should be null since `gzip` doesn't require any dict");
      dataValidator.run();

      // Reenable dict compression
      controllerClient.updateStore(
          storeName,
          new UpdateStoreQueryParams().setCompressionStrategy(CompressionStrategy.ZSTD_WITH_DICT));
      // 3rd empty push with zstd dict enabled
      runVPJ(vpjProperties, 3, controllerClient);
      ByteBuffer dictForVersion3 = getDictFromTopic(Version.composeKafkaTopic(storeName, 3), vpjProperties);
      assertNotNull(dictForVersion3, "Dict shouldn't be null for empty push to a hybrid store");
      assertNotEquals(
          dictForVersion3,
          dictForVersion1,
          "Dict built with a current version will be different from the dummy dict built in the very first empty push");
      dataValidator.run();

      // 4th empty push with zstd dict enabled
      runVPJ(vpjProperties, 4, controllerClient);
      ByteBuffer dictForVersion4 = getDictFromTopic(Version.composeKafkaTopic(storeName, 4), vpjProperties);
      assertNotNull(dictForVersion4, "Dict shouldn't be null for empty push to a hybrid store");
      assertEquals(dictForVersion4, dictForVersion3, "Venice Push Job should build a same dict for the same input");
      dataValidator.run();
    }
  }
}
