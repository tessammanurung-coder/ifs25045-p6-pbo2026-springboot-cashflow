package org.delcom.ifs25045springbootcashflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Memberi cache panjang pada berkas hasil build frontend Vaadin.
 *
 * <p>Nama berkas di {@code /VAADIN/build/} sudah memuat hash isi, sehingga aman disimpan browser
 * selama satu tahun. Tanpa filter ini Vaadin hanya memberi cache satu jam untuk berkas tersebut.
 */
@Component
public class StaticAssetCacheFilter extends OncePerRequestFilter {

    static final String IMMUTABLE_CACHE = "public, max-age=31536000, immutable";

    private final String pathPrefix;

    /**
     * @param pathPrefix awalan URL berkas build yang aman di-cache lama; dapat diubah lewat
     *                   properti {@code app.static-cache.path-prefix}
     */
    public StaticAssetCacheFilter(@Value("${app.static-cache.path-prefix:/VAADIN/build/}") String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(pathPrefix);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, new ImmutableCacheResponse(response));
    }

    /** Menimpa header Cache-Control apa pun yang ditulis Vaadin untuk berkas build. */
    private static final class ImmutableCacheResponse extends HttpServletResponseWrapper {

        ImmutableCacheResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, valueFor(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, valueFor(name, value));
        }

        private static String valueFor(String name, String value) {
            return HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(name) ? IMMUTABLE_CACHE : value;
        }
    }
}