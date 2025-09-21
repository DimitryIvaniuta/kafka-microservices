package com.github.dimitryivaniuta.gateway.producer.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Per-request correlation/trace ID propagation.
 * - Reads X-Request-Id / X-Correlation-Id if present; otherwise generates a UUID.
 * - Stores in SLF4J MDC under key "traceId" so logs and Kafka headers can include it.
 * - Echoes "X-Trace-Id" header back to the client for debugging.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID     = "X-Request-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_TRACE_ID       = "X-Trace-Id";
    public static final String MDC_KEY               = "traceId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = firstNonBlank(
                request.getHeader(HEADER_REQUEST_ID),
                request.getHeader(HEADER_CORRELATION_ID),
                request.getHeader(HEADER_TRACE_ID)
        );
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, traceId);
        response.setHeader(HEADER_TRACE_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v == null) continue;
            String t = v.trim();
            if (!t.isEmpty()) return t;
        }
        return null;
    }
}
