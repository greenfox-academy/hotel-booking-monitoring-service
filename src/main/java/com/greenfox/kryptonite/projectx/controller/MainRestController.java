package com.greenfox.kryptonite.projectx.controller;

import com.greenfox.kryptonite.projectx.model.hotelservices.HotelServiceStatusList;
import com.greenfox.kryptonite.projectx.model.BookingStatus;
import com.greenfox.kryptonite.projectx.model.pageviews.PageViewFormat;
import com.greenfox.kryptonite.projectx.repository.EventToDatabaseRepository;
import com.greenfox.kryptonite.projectx.repository.HeartbeatRepository;
import com.greenfox.kryptonite.projectx.service.FunnelService;
import com.greenfox.kryptonite.projectx.service.JsonAssemblerService;
import com.greenfox.kryptonite.projectx.service.MonitoringService;
import com.greenfox.kryptonite.projectx.service.PageViewService;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


@RestController
public class MainRestController {

  @Autowired
  private HeartbeatRepository heartbeatRepository;

  @Autowired
  private MonitoringService monitoringService;

  @Autowired
  private EventToDatabaseRepository eventToDatabaseRepository;

  @Autowired
  private PageViewService pageViewService;

  @Autowired
  private FunnelService funnelService;

  private JsonAssemblerService assembler = new JsonAssemblerService();
  private final String RABBIT_MQ_URL = System.getenv("RABBITMQ_BIGWIG_RX_URL");
  private final String EXCHANGE_NAME = "log";
  private RestTemplate restTemplate = new RestTemplate();


  @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
  public BookingStatus heartbeat(HttpServletRequest request) throws Exception {
    return monitoringService.databaseCheck(heartbeatRepository);
  }

  @RequestMapping(value = "/pageviews", method = RequestMethod.GET)
  public PageViewFormat pageviews(@RequestParam(name = "page", required = false) String page, @RequestParam(name = "path", required =  false) String path, @RequestParam(name = "min", required =  false) Integer min, @RequestParam(name = "max", required =  false) Integer max)
      throws Exception {
    pageViewService
        .addAttributeToDatabase(eventToDatabaseRepository, RABBIT_MQ_URL, EXCHANGE_NAME, "events",
            false, true);
    return assembler.returnPageView(eventToDatabaseRepository, pageViewService.returnPageIndex(page), path, min, max);
  }

  @RequestMapping(value = "/monitor", method = RequestMethod.GET)
  public HotelServiceStatusList monitor(HttpServletRequest request) throws IOException {
    return monitoringService.monitoring(restTemplate);
  }

  @PostMapping(value = "/api/funnels")
  public String createFunnel () {
    return "Funnel has been created with id" + funnelService.createAndSaveFunnelFormat();
  }
}
