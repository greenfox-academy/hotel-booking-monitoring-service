package com.greenfox.kryptonite.projectx.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.greenfox.kryptonite.projectx.model.Timestamp;
import com.greenfox.kryptonite.projectx.service.MessageQueueService;
import com.greenfox.kryptonite.projectx.repository.HeartbeatRepository;

import com.greenfox.kryptonite.projectx.service.MonitoringService;
import java.nio.charset.Charset;

import com.greenfox.kryptonite.projectx.ProjectxApplication;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectxApplication.class)
@WebAppConfiguration
@EnableWebMvc
public class MainRestControllerTest {

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      Charset.forName("utf8"));

  private boolean isItWorking = true;
  private MockMvc mockMvc;
  private HeartbeatRepository heartbeatRepositoryMock;
  private MonitoringService service;
  private HeartbeatRepository nullRepo;

  @Autowired
  private MessageQueueService messageQueueService;

  @MockBean
  HeartbeatRepository heartbeatRepository;


  @Autowired
  private WebApplicationContext webApplicationContext;


  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    this.heartbeatRepositoryMock = Mockito.mock(HeartbeatRepository.class);
    this.service = new MonitoringService();
    this.messageQueueService = new MessageQueueService();
  }

  @Test
  public void testGetEndpoint() throws Exception {
    mockMvc.perform(get("/heartbeat"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.status", is("ok")));
  }

  @Test
  public void testResponseWhenDatabaseIsNull() throws Exception {
    assertEquals((service.databaseCheck(nullRepo)).getStatus(), "ok");
  }

  @Test
  public void testResponseWhenNoElementInDatabase() throws Exception {
    Mockito.when(heartbeatRepositoryMock.count()).thenReturn(0L);
    assertEquals((service.databaseCheck(heartbeatRepositoryMock)).getDatabase(),
        "error");
  }

  @Test
  public void testResponseWhenElementInDatabase() throws Exception {
    Mockito.when(heartbeatRepositoryMock.count()).thenReturn(3L);
    assertEquals(((service.databaseCheck(heartbeatRepositoryMock)).getDatabase()), "ok");
  }


  @Test
  public void testGetEndpointWithFilledDatabase() throws Exception {
    BDDMockito.given(heartbeatRepository.count()).willReturn(1L);
    mockMvc.perform(get("/heartbeat"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.status", is("ok")))
        .andExpect(jsonPath("$.database", is("ok")));
  }

  @Test
  public void testGetEndpointWithEmptyDatabase() throws Exception {
    BDDMockito.given(heartbeatRepository.count()).willReturn(0L);
    mockMvc.perform(get("/heartbeat"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.status", is("ok")))
        .andExpect(jsonPath("$.database", is("error")));
  }

  @Test
  public void testRabbitMQConsume() throws Exception {
    messageQueueService.consume();
    assertTrue(isItWorking);
  }

  @Test
  public void testRabbitMQSend() throws Exception {
    messageQueueService.send("Mukodj!");
    assertTrue(isItWorking);
  }

  @Test
  public void testLogWithMockTime() {

    Timestamp time = new Timestamp();
    String date = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss").format(new Date());

    assertEquals(date, time.getDate());
  }

  @Test
  public void testRabbitMqConsumeParadox() throws Exception {
    messageQueueService.send("WORKING");
    messageQueueService.consume();
    String requestedMessage = messageQueueService.getTemporaryMessage();
    System.out.println(requestedMessage);
    assertTrue(!requestedMessage.equals("This isn't working!"));
  }

  @Test
  public void testQueuedMessageCount() throws Exception {
    assertTrue(messageQueueService.getCount("kryptonite") == 0);
  }
}