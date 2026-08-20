package com.example.grpcnetty;

import com.example.grpcnetty.grpc.helloworld.GreeterGrpc;
import com.example.grpcnetty.grpc.helloworld.HelloReply;
import com.example.grpcnetty.grpc.helloworld.HelloRequest;
import com.example.grpcnetty.grpc.streaming.StreamReply;
import com.example.grpcnetty.grpc.streaming.StreamRequest;
import com.example.grpcnetty.grpc.streaming.StreamingGreeterGrpc;
import com.newrelic.api.agent.Trace;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ExampleClient {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final StreamingGreeterGrpc.StreamingGreeterStub streamingStub;

    public ExampleClient(String host, int port) {
        this.channel = NettyChannelBuilder.forAddress(host, port)
                .intercept(new RequestIdClientInterceptor())
                .usePlaintext()
                .build();
        this.blockingStub = GreeterGrpc.newBlockingStub(channel);
        this.streamingStub = StreamingGreeterGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Trace(dispatcher = true)
    public void callSayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply reply = blockingStub.sayHello(request);
        System.out.println("[client] SayHello -> " + reply.getMessage());
    }

    @Trace(dispatcher = true)
    public void callThrowException(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        try {
            blockingStub.throwException(request);
        } catch (StatusRuntimeException e) {
            System.out.println("[client] ThrowException -> expected failure: " + e.getStatus());
        }
    }

    @Trace(dispatcher = true)
    public void callStreaming(String name) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        StreamObserver<StreamRequest> requestStream = streamingStub.sayHelloStreaming(new StreamObserver<StreamReply>() {
            @Override
            public void onNext(StreamReply value) {
                System.out.println("[client] stream onNext <- " + value.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("[client] stream onError: " + t);
                done.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("[client] stream onCompleted");
                done.countDown();
            }
        });

        for (int i = 0; i < 3; i++) {
            requestStream.onNext(StreamRequest.newBuilder().setName(name + "-" + i).build());
        }
        requestStream.onCompleted();
        done.await(10, TimeUnit.SECONDS);
    }
}
