package com.orders;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import com.orders.utilities.ClusterConfigUtil;

public class OrderVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    VertxOptions options = new VertxOptions();
    Config hazelcastConfig = new Config();
    // Load configuration from file
    options.setClusterManager(new HazelcastClusterManager(ClusterConfigUtil.loadConfig()));

    options.setClusterManager(new HazelcastClusterManager());

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        vertx.deployVerticle(new OrderVerticle());
      } else {
        // Handle failure
      }
    });
  }

  @Override
  public void start() {
    EventBus eventBus = vertx.eventBus();

    // Event bus consumer for handling incoming orders
    eventBus.consumer("addOrder", message -> {
      // Process the order
      JsonObject order = (JsonObject) message.body();
      // Save the order details (e.g., in-memory or file-based storage)
      System.out.println("Received Order: " + order);
      // Acknowledge the successful processing of the order
      message.reply("Order processed successfully");
    });

    // Event bus consumer for handling get orders request
    eventBus.consumer("getOrders", message -> {
      // Retrieve and send orders (e.g., from in-memory or file-based storage)
      // For simplicity, let's assume orders are stored as a JSON array
      JsonObject orders = new JsonObject();
      orders.put("orders", new JsonArray());
      message.reply(orders);
    });
  }
}
