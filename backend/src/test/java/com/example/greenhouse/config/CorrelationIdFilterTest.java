package com.example.greenhouse.config;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void doFilter_generatesCorrelationId_whenMissing() throws ServletException, IOException {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse resp = new MockHttpServletResponse();
    FilterChain chain = (request, response) -> {
      assertNotNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
      assertEquals(16, MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY).length());
    };

    filter.doFilter(req, resp, chain);
    assertNotNull(resp.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
  }

  @Test
  void doFilter_preservesExistingCorrelationId() throws ServletException, IOException {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "existing-id-1234");
    MockHttpServletResponse resp = new MockHttpServletResponse();
    FilterChain chain = (request, response) -> {
      assertEquals("existing-id-1234", MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    };

    filter.doFilter(req, resp, chain);
    assertEquals("existing-id-1234", resp.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
  }

  @Test
  void doFilter_clearsMdcAfterRequest() throws ServletException, IOException {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse resp = new MockHttpServletResponse();
    FilterChain chain = (request, response) -> {};

    filter.doFilter(req, resp, chain);
    assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
  }
}
