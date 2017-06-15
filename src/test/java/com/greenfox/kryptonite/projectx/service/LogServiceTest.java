package com.greenfox.kryptonite.projectx.service;

import com.greenfox.kryptonite.projectx.model.Log;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogServiceTest {

  private LogService logging;

  @Before
  public void setup() throws Exception {
    this.logging = new LogService();
  }

  @Test
  public void testDebugLogging() {
    assertEquals(logging.log("DEBUG", "message"), 500);
  }

  @Test
  public void testInfoLogging() {
    assertEquals(logging.log("INFO", "message"), 400);
  }

  @Test
  public void testWarnLogging() {
    assertEquals(logging.log("WARN", "message"), 300);
  }

  @Test
  public void testErrorLogging() {
    assertEquals(logging.log("ERROR", "message"), 200);
  }

}