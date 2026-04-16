package com.gabriel.gatewayserver.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final Tracer tracer;

    public RequestLoggingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.nanoTime();
        var method = exchange.getRequest().getMethod();
        var path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    int statusCode = status != null ? status.value() : 200;
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    Span currentSpan = tracer.currentSpan();
                    String traceId = currentSpan != null ? currentSpan.context().traceId() : "n/a";
                    String spanId = currentSpan != null ? currentSpan.context().spanId() : "n/a";

                    log.info(
                            "Gateway request {} {} -> {} in {} ms traceId={} spanId={}",
                            method,
                            path,
                            statusCode,
                            elapsedMs,
                            traceId,
                            spanId
                    );
                });
    }
}
