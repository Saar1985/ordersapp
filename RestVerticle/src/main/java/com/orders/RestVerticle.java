package com.orders;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import com.orders.utilities.ClusterConfigUtil;

public class RestVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    VertxOptions options = new VertxOptions();
    Config hazelcastConfig = new Config();
    // Load configuration from file
    options.setClusterManager(new HazelcastClusterManager(ClusterConfigUtil.loadConfig()));

    options.setClusterManager(new HazelcastClusterManager());

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        vertx.deployVerticle(new RestVerticle());
      } else {
        // Handle failure
      }
    });
  }

  @Override
  public void start() {
    EventBus eventBus = vertx.eventBus();

    // REST API endpoints
    vertx.createHttpServer()
      .requestHandler(req -> {
        if (req.method().equals("POST") && req.path().equals("/login")) {
          // Handle Login
          // Get username and password from the request body
          String username = req.getParam("username");
          String password = req.getParam("password");

          // Check login and open a session
          boolean loginResult = loginUser(username, password);
          req.response().end(String.valueOf(loginResult));
        } else if (req.method().equals("POST") && req.path().equals("/logout")) {
          // Handle Logout
          // Close the session
          boolean logoutResult = logoutUser();
          req.response().end(String.valueOf(logoutResult));
        } else if (req.method().equals("POST") && req.path().equals("/addOrder")) {
          // Handle AddOrder
          // Get order from the request body
          req.bodyHandler(buffer -> {
            JsonObject order = buffer.toJsonObject();

            // Send the order to OrderVerticle
            eventBus.request("addOrder", order, reply -> {
              if (reply.succeeded()) {
                req.response().end("Order added successfully");
              } else {
                req.response().end("Failed to add order");
              }
            });
          });
        } else if (req.method().equals("GET") && req.path().equals("/getOrders")) {
          // Handle GetOrders
          // Retrieve orders from OrderVerticle
          eventBus.request("getOrders", "", reply -> {
            if (reply.succeeded()) {
              req.response().end(reply.result().body().toString());
            } else {
              req.response().end("Failed to get orders");
            }
          });
        } else {
          req.response().setStatusCode(404).end();
        }
      })
      .listen(8080);
  }

  private boolean loginUser(String username, String password) {
    // Implement login logic and session handling
    // Save username and session details (e.g., in-memory or file-based storage)
    return true; // Placeholder, implement based on your requirements
  }

  private boolean logoutUser() {
    // Implement logout logic and session handling
    // Close the session (e.g., remove session details from storage)
    return true; // Placeholder, implement based on your requirements
  }
}
