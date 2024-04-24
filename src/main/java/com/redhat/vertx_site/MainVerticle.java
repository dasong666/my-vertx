package com.redhat.vertx_site;

import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerResponse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        // Create an HTTP server
        HttpServer server = vertx.createHttpServer();

        // Create a router to handle the HTTP requests
        Router router = Router.router(vertx);

        // Log all requests
        router.route().handler(LoggerHandler.create());

        // Health check setup
        HealthChecks healthChecks = HealthChecks.create(vertx);
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.createWithHealthChecks(healthChecks);

        // Register a simple server-up health check
        healthChecks.register("server-up", promise -> promise.complete());

        // Actuator endpoints
        router.get("/actuator/health").handler(healthCheckHandler);

        // Define the route for the current time
        router.get("/time").handler(this::handleRoot);

        // Handle not found
        router.route().handler(rc -> rc.response().setStatusCode(404).end("Not Found"));

        // Start the server and listen on port 8000
        server.requestHandler(router).listen(8000, result -> {
            if (result.succeeded()) {
                System.out.println("Server listening at port 8000");
            } else {
                System.out.println("Failed to start the server: " + result.cause().getMessage());
            }
        });
    }

    private void handleRoot(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        // Set the content type header
        response.putHeader("Content-Type", "application/json; charset=utf-8");

        // Get current time and format it
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Send formatted current time as the response
        response.end("{\"Order Date\": \"" + formattedDate + "\"}");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}

