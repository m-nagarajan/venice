package com.linkedin.venice.pushmonitor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedin.venice.client.store.transport.TransportClient;
import com.linkedin.venice.client.store.transport.TransportClientResponse;
import com.linkedin.venice.routerapi.PushStatusResponse;
import com.linkedin.venice.samza.VeniceSystemFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.apache.samza.system.SystemProducer;
import org.testng.annotations.Test;


public class RouterBasedPushMonitorTest {
  @Test
  public void testHandleError() throws IOException {
    TransportClient transportClient = mock(TransportClient.class);
    CompletableFuture<TransportClientResponse> responseFuture = new CompletableFuture<>();
    TransportClientResponse transportClientResponse = new TransportClientResponse(1, null, "testBody".getBytes());
    responseFuture.complete(transportClientResponse);
    when(transportClient.get(anyString())).thenReturn(responseFuture);

    ObjectMapper mockMAPPER = mock(ObjectMapper.class);
    PushStatusResponse pushStatusResponse = new PushStatusResponse();
    pushStatusResponse.setExecutionStatus(ExecutionStatus.ERROR);
    when(mockMAPPER.readValue(eq(transportClientResponse.getBody()), eq(PushStatusResponse.class)))
        .thenReturn(pushStatusResponse);

    VeniceSystemFactory factory = mock(VeniceSystemFactory.class);
    SystemProducer producer = mock(SystemProducer.class);

    // Create the monitor and task
    RouterBasedPushMonitor monitor = new RouterBasedPushMonitor(transportClient, "topic", factory, producer);
    RouterBasedPushMonitor.PushMonitorTask task = monitor.getPushMonitorTask();
    task.setMapper(mockMAPPER);

    // Execute the task logic
    task.run();

    // Verify expected behavior
    verify(factory).endStreamReprocessingSystemProducer(producer, false);
    assertFalse(task.isRunning.get()); // Task should continue polling
    task.close();
    monitor.close();
  }
}
