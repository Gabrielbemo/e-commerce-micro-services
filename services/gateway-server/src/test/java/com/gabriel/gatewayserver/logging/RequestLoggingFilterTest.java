package com.gabriel.gatewayserver.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

    @Test
    void shouldLogCompletedRequestWithHttpStatusAndTraceContext(CapturedOutput output) {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("trace-123");
        when(context.spanId()).thenReturn("span-456");

        RequestLoggingFilter filter = new RequestLoggingFilter(tracer);
        ServerWebExchange exchange = exchange(HttpStatus.CREATED);
        WebFilterChain chain = ignored -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(output.getOut())
                .contains("Gateway request GET /api/v1/products -> 201")
                .contains("traceId=trace-123")
                .contains("spanId=span-456");
    }

    @Test
    void shouldUseDefaultsWhenStatusAndTraceContextAreUnavailable(CapturedOutput output) {
        Tracer tracer = mock(Tracer.class);
        RequestLoggingFilter filter = new RequestLoggingFilter(tracer);
        ServerWebExchange exchange = exchange(null);
        WebFilterChain chain = ignored -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(output.getOut())
                .contains("Gateway request GET /api/v1/products -> 200")
                .contains("traceId=n/a")
                .contains("spanId=n/a");
    }

    private ServerWebExchange exchange(HttpStatus status) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/api/v1/products"));

        ServerHttpResponse response = mock(ServerHttpResponse.class);
        when(response.getStatusCode()).thenReturn(status);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        return exchange;
    }
}
