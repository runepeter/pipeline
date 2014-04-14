package org.brylex.pipeline;

import org.vertx.java.core.*;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.IOException;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {

        /*final Vertx vertx = VertxFactory.newVertx();

        final RouteMatcher router = new RouteMatcher();
        router.get("/jobs", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {

            }
        });


        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {

            }
        }).listen(1234);

        // Prevent the JVM from exiting
        System.in.read();*/

        //System.setProperty("org.vertx.logger-delegate-factory-class-name", "org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory");

        JsonObject configuration = new JsonObject().putString("foo", "wibble");

        final PlatformManager pm = PlatformLocator.factory.createPlatformManager();

        pm.deployVerticle("org.brylex.pipeline.rest.RestVerticle", configuration, new URL[]{}, 5, null, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                System.err.println("RestVerticle deployed...");
            }
        });

        pm.deployVerticle("org.brylex.pipeline.PingVerticle", configuration, new URL[]{}, 1, null, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                System.err.println("PingVerticle deployed...");
            }
        });

        pm.deployVerticle("org.brylex.pipeline.JbpmVerticle", configuration, new URL[]{}, 1, null, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                System.err.println("JbpmVerticle deployed...");
            }
        });

        System.in.read();
    }
}
