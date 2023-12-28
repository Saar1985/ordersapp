package com.orders.managers;

import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderManager {

  private static final String ORDERS_FILE_PATH = "/resources/orders/orderdata.csv";
  private static final Logger logger = LogManager.getLogger(OrderManager.class);
  private FileSystem fileSystem;

  public OrderManager(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    validateFileExists();
  }

  public void addOrder(JsonObject order) {

    JsonObject ordersJson = fileSystem.readFileBlocking(ORDERS_FILE_PATH).toJsonObject();

    JsonArray orders = ordersJson.getJsonArray("orders", new JsonArray());
    orders.add(order);

    fileSystem.writeFileBlocking(ORDERS_FILE_PATH, ordersJson.put("orders", orders).toBuffer());
  }

  public JsonArray getOrdersForUser(String username) {

    JsonObject ordersJson = fileSystem.readFileBlocking(ORDERS_FILE_PATH).toJsonObject();
    JsonArray allOrders = ordersJson.getJsonArray("orders", new JsonArray());

    JsonArray userOrders = new JsonArray();
    for (Object orderObj : allOrders) {
      JsonObject order = (JsonObject) orderObj;
      if (order.getString("username").equals(username)) {
        userOrders.add(order);
      }
    }

    return userOrders;
  }

  public void validateFileExists()
  {
    if (!fileSystem.existsBlocking(ORDERS_FILE_PATH)) {

      fileSystem.createFile(ORDERS_FILE_PATH, handler -> {
        if (handler.succeeded()) {
          logger.info("Successfully created file: " + ORDERS_FILE_PATH);
        } else {
          logger.info("Failure in creating file: " + ORDERS_FILE_PATH);
        }
      });
      fileSystem.writeFileBlocking(ORDERS_FILE_PATH, new JsonObject().put("orders", new JsonArray()).toBuffer());
    }
  }
}
