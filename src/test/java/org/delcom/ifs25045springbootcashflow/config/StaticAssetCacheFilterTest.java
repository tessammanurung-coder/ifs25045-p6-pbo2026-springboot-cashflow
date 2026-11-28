package org.delcom.ifs25045springbootcashflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticAssetCacheFilterTest {

    private static final String BUILD_PATH = "/VAADIN/build/";
    private static final String BUILD_FILE = BUILD_PATH + "app-abc123.js";
    private static final String OTHER_HEADER = "X-Other";

    private final StaticAssetCacheFilter filter = new StaticAssetCacheFilter(BUILD_PATH);

    @Test
    void setHeaderOnBuildFileForcesImmutableCache() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            HttpServletResponse http = (HttpServletResponse) res;
            http.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=3600");
            http.setHeader(OTHER_HEADER, "a");
        };

        filter.doFilter(new MockHttpServletRequest("GET", BUILD_FILE), response, chain);

        assertEquals(StaticAssetCacheFilter.IMMUTABLE_CACHE, response.getHeader(HttpHeaders.CACHE_CONTROL));
        assertEquals("a", response.getHeader(OTHER_HEADER));
    }

    @Test
    void addHeaderOnBuildFileForcesImmutableCache() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            HttpServletResponse http = (HttpServletResponse) res;
            http.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            http.addHeader(OTHER_HEADER, "b");
        };

        filter.doFilter(new MockHttpServletRequest("GET", BUILD_FILE), response, chain);

        assertEquals(StaticAssetCacheFilter.IMMUTABLE_CACHE, response.getHeader(HttpHeaders.CACHE_CONTROL));
        assertEquals("b", response.getHeader(OTHER_HEADER));
    }

    @Test
    void otherPathsKeepTheirOwnCacheHeader() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                ((HttpServletResponse) res).setHeader(HttpHeaders.CACHE_CONTROL, "max-age=3600");

        filter.doFilter(new MockHttpServletRequest("GET", "/styles.css"), response, chain);

        assertEquals("max-age=3600", response.getHeader(HttpHeaders.CACHE_CONTROL));
    }
}