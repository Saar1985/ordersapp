package com.orders;

import com.hazelcast.config.Config;
import com.orders.managers.OrderManager;
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

  private OrderManager orderManager;
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
    orderManager = new OrderManager(vertx.fileSystem());

    eventBus.consumer("addOrder", message -> {
      JsonObject order = (JsonObject) message.body();
      orderManager.addOrder(order);
      message.reply("Order added successfully");
    });

    eventBus.consumer("getOrders", message -> {
      String username = (String) message.body();
      JsonArray userOrders = orderManager.getOrdersForUser(username);
      message.reply(userOrders);
    });
  }
}
