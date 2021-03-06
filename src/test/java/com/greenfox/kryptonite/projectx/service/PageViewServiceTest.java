package com.greenfox.kryptonite.projectx.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfox.kryptonite.projectx.model.pageviews.EventToDatabase;
import com.greenfox.kryptonite.projectx.model.pageviews.HotelEventQueue;
import com.greenfox.kryptonite.projectx.repository.EventToDatabaseRepository;
import com.greenfox.kryptonite.projectx.repository.HeartbeatRepository;


import java.nio.charset.Charset;

import com.greenfox.kryptonite.projectx.HotelMonitoringApplication;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HotelMonitoringApplication.class)
@WebAppConfiguration
@EnableWebMvc
public class PageViewServiceTest {

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      Charset.forName("utf8"));

  private final String RABBIT_MQ_URL = System.getenv("RABBITMQ_BIGWIG_RX_URL");
  private final String EXCHANGE_NAME = "log2";
  private MockMvc mockMvc;
  private EventToDatabaseRepository eventToDatabaseRepositoryMock;
  private PageViewService pageViewService;
  private HeartbeatRepository nullRepo;
  private MessageQueueService messageQueueService;
  private ObjectMapper objectMapper;
  private HotelEventQueue hotelEventQueue;

  @Autowired
  WebApplicationContext webApplicationContext;

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    this.eventToDatabaseRepositoryMock = Mockito.mock(EventToDatabaseRepository.class);
    this.pageViewService = new PageViewService();
    this.messageQueueService = new MessageQueueService();
    this.objectMapper = new ObjectMapper();
    this.hotelEventQueue = new HotelEventQueue("test-hotelEventQueue", "/testPath", "5431325134");
  }

  @Test
  public void testCreateObjectFromJson() throws Exception {
    assertEquals(hotelEventQueue.toString(), pageViewService.createObjectFromJson(
        "{\"type\":\"test-hotelEventQueue\",\"path\":\"/testPath\",\"trackingId\":\"5431325134\"}")
        .toString());
  }

  @Test
  public void testCreateObjectFromJsonNotEquals() throws Exception {
    assertNotEquals("not equals", pageViewService.createObjectFromJson(
        "{\"type\":\"test-hotelEventQueue\",\"path\":\"/testPath\",\"trackingId\":\"5431325134\"}")
        .toString());
  }

  @Test
  public void testCreateObjectFromJsonWithQueue() throws Exception {
    messageQueueService.send(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", "nothing important");
    messageQueueService.consume(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", true, true);
    String temp = messageQueueService.getTemporaryMessage();
    pageViewService.createObjectFromJson(temp);
    assertEquals(pageViewService.createObjectFromJson(temp).toString(), hotelEventQueue.toString());
  }

  @Test
  public void testAddAttributeToEmptyDatabaseWithQueue() throws Exception {
    messageQueueService.send(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", "nothing important");
    Iterable<EventToDatabase> list = Arrays.asList();
    Mockito.when(eventToDatabaseRepositoryMock.findAll()).thenReturn(list);
    assertFalse(pageViewService
        .addAttributeToDatabase(eventToDatabaseRepositoryMock, RABBIT_MQ_URL, EXCHANGE_NAME,
            "testEventQueue", true, true));
  }

  @Test
  public void testAddAttributeToNoTEmptyDatabaseWithQueue() throws Exception {
    messageQueueService.send(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", "nothing important");
    Iterable<EventToDatabase> list = Arrays
        .asList(new EventToDatabase(hotelEventQueue.getPath(), hotelEventQueue.getType()));
    Mockito.when(eventToDatabaseRepositoryMock.findAll()).thenReturn(list);
    assertTrue(pageViewService
        .addAttributeToDatabase(eventToDatabaseRepositoryMock, RABBIT_MQ_URL, EXCHANGE_NAME,
            "testEventQueue", true, true));
  }

  @Test
  public void testConsumeHotelEventQueue() throws Exception {
    messageQueueService.send(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", "nothing important");
    HotelEventQueue hotelEventQueueForTests = pageViewService
        .consumeHotelEventQueue(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", true, true);
    assertEquals(hotelEventQueueForTests.toString(), hotelEventQueue.toString());
  }

  @Test
  public void testConsumeHotelEventQueueNotEquals() throws Exception {
    messageQueueService.send(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", "nothing important");
    HotelEventQueue hotelEventQueueForTests = pageViewService
        .consumeHotelEventQueue(RABBIT_MQ_URL, EXCHANGE_NAME, "testEventQueue", true, true);
    assertNotEquals(hotelEventQueueForTests.toString(), "just some random event");
  }

  @Test
  public void testCheckEventDatabaseWithExistingElementsInDB() throws Exception {
    List<EventToDatabase> testList1 = Arrays
        .asList(new EventToDatabase(hotelEventQueue.getPath(), hotelEventQueue.getType()));
    Mockito.when(eventToDatabaseRepositoryMock.count()).thenReturn(1L);
    assertTrue(pageViewService
        .checkEventDatabase(eventToDatabaseRepositoryMock, hotelEventQueue, testList1));
  }

  @Test
  public void testCheckEventDatabaseWithoutExistingElementsInDB() throws Exception {
    List<EventToDatabase> testList2 = Arrays
        .asList(new EventToDatabase(hotelEventQueue.getPath() + "mock", hotelEventQueue.getType()));
    Mockito.when(eventToDatabaseRepositoryMock.count()).thenReturn(1L);
    assertFalse(pageViewService
        .checkEventDatabase(eventToDatabaseRepositoryMock, hotelEventQueue, testList2));
  }

  @Test
  public void testSaveEventToDatabase() throws Exception {
    EventToDatabase eventToDatabase = new EventToDatabase("/testPath", "test-hotelEventQueue", 1);
    assertEquals(eventToDatabase.toString(),
        pageViewService.saveEventToDatabase(eventToDatabaseRepositoryMock, hotelEventQueue)
            .toString());
  }

  @Test
  public void testUpdateEventToDatabaseWithCorrectInput() throws Exception {
    EventToDatabase eventToDatabase = new EventToDatabase(hotelEventQueue.getPath(),
        hotelEventQueue.getType());
    pageViewService.updateEventInDatabase(eventToDatabaseRepositoryMock, eventToDatabase);
    assertEquals(2, eventToDatabase.getCount());
  }

  @Test
  public void testSendJsonHotelEventQueue() throws Exception {
    assertEquals(pageViewService.sendJsonHotelEventQueue(),
        objectMapper.writeValueAsString(hotelEventQueue));
  }

  @Test
  public void testReturnPageIndexEquals() {
    assertEquals(6,
        pageViewService.returnPageIndex("6"));
  }

  @Test
  public void testReturnPageIndexWithNotEquals() {
    assertNotEquals(5,
        pageViewService.returnPageIndex("6"));
  }

  @Test
  public void testUpdateEventToDatabase() {
    EventToDatabase eventToDatabase = new EventToDatabase("path", "type");
    pageViewService.updateEventInDatabase(eventToDatabaseRepositoryMock, eventToDatabase);
    assertEquals(eventToDatabase.getCount() + 1,
        pageViewService.updateEventInDatabase(eventToDatabaseRepositoryMock, eventToDatabase)
            .getCount());
  }

  @Test
  public void testUpdateEventToDatabaseNotEquals() {
    EventToDatabase eventToDatabase = new EventToDatabase("path", "type");
    pageViewService.updateEventInDatabase(eventToDatabaseRepositoryMock, eventToDatabase);
    assertNotEquals(eventToDatabase.getCount(),
        pageViewService.updateEventInDatabase(eventToDatabaseRepositoryMock, eventToDatabase)
            .getCount());
  }
}
