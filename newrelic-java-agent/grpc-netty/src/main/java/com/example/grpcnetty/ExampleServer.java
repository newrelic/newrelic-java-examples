package com.example.grpcnetty;

import com.example.grpcnetty.grpc.helloworld.GreeterGrpc;
import com.example.grpcnetty.grpc.helloworld.HelloReply;
import com.example.grpcnetty.grpc.helloworld.HelloRequest;
import com.example.grpcnetty.grpc.streaming.StreamReply;
import com.example.grpcnetty.grpc.streaming.StreamRequest;
import com.example.grpcnetty.grpc.streaming.StreamingGreeterGrpc;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ExampleServer {
    private final int port;
    private Server server;

    public ExampleServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .addService(new StreamingGreeterImpl())
                .build()
                .start();
        System.out.println("[server] grpc-netty server listening on port " + server.getPort());
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public int getPort() {
        return server != null ? server.getPort() : port;
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            System.out.println("[server] SayHello name=" + req.getName());

            responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + req.getName()).build());
            responseObserver.onCompleted();
        }

        @Override
        public void throwException(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            System.out.println("[server] ThrowException name=" + req.getName() + " (deliberately failing)");

            int boom = 7 / (req.getName().isEmpty() ? 0 : 0);
            responseObserver.onNext(HelloReply.newBuilder().setMessage("unreachable " + boom).build());
            responseObserver.onCompleted();
        }
    }

    static class StreamingGreeterImpl extends StreamingGreeterGrpc.StreamingGreeterImplBase {
        @Override
        public StreamObserver<StreamRequest> sayHelloStreaming(StreamObserver<StreamReply> responseObserver) {
            return new StreamObserver<StreamRequest>() {
                @Override
                public void onNext(StreamRequest value) {
                    System.out.println("[server] stream onNext name=" + value.getName());
                    responseObserver.onNext(StreamReply.newBuilder().setMessage("Hello " + value.getName()).build());
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("[server] stream onError: " + t);
                }

                @Override
                public void onCompleted() {
                    System.out.println("[server] stream onCompleted");
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
