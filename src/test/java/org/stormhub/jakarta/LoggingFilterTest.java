package org.stormhub.jakarta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.MarkerFactory.getMarker;

@SuppressWarnings({"squid:S00100", "squid:S00112", "squid:S3457"})
@ExtendWith({MockitoExtension.class})
class LoggingFilterTest {
    @InjectMocks
    private LoggingFilter loggingFilter = new LoggingFilter();
    @Mock
    private Logger logger;
    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;
    private MockFilterChain filterChain;

    private static class TestFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) {
            // not used
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            response.getOutputStream().write("Test response body".getBytes());
        }

        @Override
        public void destroy() {
            // not used
        }
    }

    @BeforeEach
    public void setUp() {
        httpServletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/test");
        httpServletRequest.addHeader("Accept", "application/json");
        httpServletRequest.addParameter("param1", "1000");
        httpServletRequest.setContent("Test request body".getBytes());
        httpServletRequest.setContentType(MediaType.TEXT_PLAIN_VALUE);

        httpServletResponse = new MockHttpServletResponse();
        httpServletResponse.setContentType(MediaType.TEXT_PLAIN_VALUE);

        filterChain = new MockFilterChain(new HttpRequestHandlerServlet(), new TestFilter());
    }

    @Test
    void testDoFilter_Full() throws Exception {
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);

        loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(logger).isDebugEnabled();
        verify(logger).debug(getMarker("REQUEST"), "REQUEST: {\"sender\":\"127.0.0.1\",\"method\":\"GET\",\"path\":\"http://localhost:8080/test\",\"params\":{\"param1\":\"1000\"},\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"text/plain\"},\"body\":\"Test request body\"}");
        verify(logger).debug(getMarker("RESPONSE"), "RESPONSE: {\"status\":200,\"headers\":{\"Content-Type\":\"text/plain\"},\"body\":\"Test response body\"}");
    }

    @Test
    void testDoFilter_MarkeredOnly() throws Exception {
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("disablePrefix", "true");
        loggingFilter.init(filterConfig);

        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);

        loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(logger).isDebugEnabled();
        verify(logger).debug(getMarker("REQUEST"), "{\"sender\":\"127.0.0.1\",\"method\":\"GET\",\"path\":\"http://localhost:8080/test\",\"params\":{\"param1\":\"1000\"},\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"text/plain\"},\"body\":\"Test request body\"}");
        verify(logger).debug(getMarker("RESPONSE"), "{\"status\":200,\"headers\":{\"Content-Type\":\"text/plain\"},\"body\":\"Test response body\"}");
    }

    @Test
    void testDoFilter_JsonOnly() throws Exception {
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("disablePrefix", "true");
        filterConfig.addInitParameter("disableMarker", "true");
        loggingFilter.init(filterConfig);

        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);

        loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(logger).isDebugEnabled();
        verify(logger).debug("{\"sender\":\"127.0.0.1\",\"method\":\"GET\",\"path\":\"http://localhost:8080/test\",\"params\":{\"param1\":\"1000\"},\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"text/plain\"},\"body\":\"Test request body\"}");
        verify(logger).debug("{\"status\":200,\"headers\":{\"Content-Type\":\"text/plain\"},\"body\":\"Test response body\"}");
    }
}