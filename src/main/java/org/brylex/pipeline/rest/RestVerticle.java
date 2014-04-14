package org.brylex.pipeline.rest;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Map;

public class RestVerticle extends Verticle {
    @Override
    public void start() {

        container.logger().info("Starting RestVerticle...");

        final EventBus bus = getVertx().eventBus();

        final HttpServer httpServer = getVertx().createHttpServer();
        final RouteMatcher router = new RouteMatcher();

        router.get("/jobs", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        bus.send("job.process.start", "EVENT" + event.toString(), new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                request.response().setStatusCode(200);
                                request.response().setStatusMessage(event.body().toString());
                                request.response().end();
                            }
                        });
                    }
                });
            }
        });

        router.postWithRegEx("/pipelines/(.+)/?", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {

                final String processId = request.params().get("param0");

                container.logger().info("Request for creating new [" + processId + "] pipeline...");

                bus.send("pipeline.process.new", processId, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {

                        final String json = event.body().toString();

                        request.response().setStatusCode(201);
                        request.response().putHeader("Location", "http://localhost:1234" + request.path() + "/" + event.body().getNumber("id"));
                        request.response().putHeader("Content-Length", "" + json.length());
                        request.response().write(json);
                        request.response().end();
                    }
                });
            }
        });

        router.putWithRegEx("/pipelines/(.+)/(.+)/?", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {

                container.logger().info("Request for update to instance [" + request.params().get("param1") + "] of pipeline [" + request.params().get("param0") + "].");

                request.response().end();
            }
        });

        router.getWithRegEx("/pipelines/(.+)/(.+)/?", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {

                final String processId = request.params().get("param0");
                final long instanceId = Long.parseLong(request.params().get("param1"));

                container.logger().info("Request for status of instance [" + instanceId + "] of pipeline [" + processId + "].");

                final JsonObject json = new JsonObject();
                json.putNumber("id", instanceId);

                bus.send("pipeline.process.info", json, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {

                        final String json = event.body().toString();

                        request.response().setStatusCode(200);
                        request.response().putHeader("Content-Length", "" + json.length());
                        request.response().write(json);
                        request.response().end();
                    }
                });
            }
        });

        httpServer.requestHandler(router).listen(1234);
    }
}
