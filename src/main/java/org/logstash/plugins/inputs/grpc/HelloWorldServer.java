/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.logstash.plugins.inputs.grpc;

import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Server that manages startup/shutdown of a {@code Greeter} server. */
public class HelloWorldServer {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());
    private static final OpenTelemetry openTelemetry =
            ExampleConfiguration.initializeOpenTelemetry("localhost", 9411);
    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server =
                Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                        .addService(configureServerInterceptor(openTelemetry, new GreeterImpl()))
                        .build()
                        .start();

        logger.info("Server started, listening on " + port);
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread() {
                            @Override
                            public void run() {
                                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                                try {
                                    HelloWorldServer.this.stop();
                                } catch (InterruptedException e) {
                                    e.printStackTrace(System.err);
                                }
                                System.err.println("*** server shut down");
                            }
                        });
    }

    // For server-side, attatch the interceptor to your service.
    ServerServiceDefinition configureServerInterceptor(
            OpenTelemetry openTelemetry, BindableService bindableService) {
        GrpcTelemetry grpcTelemetry = GrpcTelemetry.create(openTelemetry);
        return ServerInterceptors.intercept(bindableService, grpcTelemetry.newServerInterceptor());
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /** Await termination on the main thread since the grpc library uses daemon threads. */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /** Main launches the server from the command line. */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}