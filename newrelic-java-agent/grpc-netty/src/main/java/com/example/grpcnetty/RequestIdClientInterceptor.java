package com.example.grpcnetty;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.concurrent.atomic.AtomicLong;

public class RequestIdClientInterceptor implements ClientInterceptor {

    public static final Metadata.Key<String> REQUEST_ID_KEY =
            Metadata.Key.of("x-example-request-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        String requestId = "req-" + COUNTER.incrementAndGet();
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(REQUEST_ID_KEY, requestId);
                System.out.println("[client] " + method.getFullMethodName() + " x-example-request-id=" + requestId);
                super.start(responseListener, headers);
            }
        };
    }
}
