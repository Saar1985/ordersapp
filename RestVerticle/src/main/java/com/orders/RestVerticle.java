package com.orders;

import com.orders.managers.LoginManager;
import com.orders.pojos.LoginResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import com.hazelcast.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import com.orders.utilities.ClusterConfigUtil;

import java.util.Optional;

public class RestVerticle extends AbstractVerticle {

  LoginManager loginManager;

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
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    loginManager = new LoginManager(vertx.fileSystem());

    router.post("/login").handler(ctx -> {
      ctx.request().bodyHandler(buffer -> {
        try {
          JsonObject body = buffer.toJsonObject();

          // Extract username and password
          String username = body.getString("username");
          String password = body.getString("password");

          // Process the login
          LoginResponse loginResponse = loginManager.loginUser(username, password);

          // Respond with the login response
          ctx.response()
            .putHeader("content-type", "application/json")
            .end(JsonObject.mapFrom(loginResponse).encode());
        } catch (Exception e) {
          // Handle any exceptions (e.g., JSON parsing error)
          ctx.response().setStatusCode(400).end("Bad Request");
        }
      });
    });

    router.post("/logout").handler(ctx -> {
      String sessionId = ctx.request().getHeader("SessionId");
      boolean logoutResult = loginManager.logoutUser(sessionId);
      ctx.response().end(String.valueOf(logoutResult));
    });

    router.post("/addOrder").handler(ctx -> {
      String sessionId = ctx.request().getHeader("SessionId");

      Optional<String> username = loginManager.validateSession(sessionId);

      if(username.isEmpty())
      {
        ctx.response().end("User not logged in, sorry man");
      }
      else
      {
        ctx.request().bodyHandler(buffer -> {
          try {

            JsonObject orderJson = buffer.toJsonObject();

            // Add the username to the orderJson based on the session
            orderJson.put("username", username.get());

            eventBus.request("addOrder", orderJson, reply -> {
              if (reply.succeeded()) {
                ctx.response().end("Order added successfully");
              } else {
                ctx.response().end("Failed to add order");
              }
            });

        } catch (Exception e) {
        // Handle any exceptions (e.g., JSON parsing error)
        ctx.response().setStatusCode(400).end("Bad Request");
      }
        });
      }
    });

    router.get("/getOrders").handler(ctx -> {

      String sessionId = ctx.request().getHeader("SessionId");

      Optional<String> username = loginManager.validateSession(sessionId);

      if(username.isEmpty())
      {
        ctx.response().end("User not logged in, sorry man");
      }
      else
      {
        eventBus.request("getOrders", username.get(), reply -> {
          if (reply.succeeded()) {
            ctx.response().end(reply.result().body().toString());
          } else {
            ctx.response().end("Failed to get orders");
          }
        });
      }
    });

    server.requestHandler(router).listen(7777);
  }
}
