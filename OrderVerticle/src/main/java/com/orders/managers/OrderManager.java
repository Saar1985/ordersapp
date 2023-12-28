package com.orders.managers;

import com.orders.pojos.Order;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OrderManager {

  private static final String ORDERS_FILE_PATH = "/resources/orders/orderdata.csv";
  private static final Logger logger = LogManager.getLogger(OrderManager.class);
  private FileSystem fileSystem;

  public OrderManager(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    validateFileExists();
  }

  public void addOrder(JsonObject ordersJson) {

    try {
      Order order = ordersJson.mapTo(Order.class);

      fileSystem.open(ORDERS_FILE_PATH, new OpenOptions().setAppend(true), ar -> {
        if (ar.succeeded()) {
          AsyncFile ws = ar.result();
          ws.write(Buffer.buffer(order.toString()));
        } else {
          logger.error("Could not open file: " + ORDERS_FILE_PATH);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }
  }

  public JsonArray getOrdersForUser(String username) {

    try {
      String content = fileSystem.readFileBlocking(ORDERS_FILE_PATH).toString();
      List<String> linesList = content.lines().toList();
      JsonArray jsonArray = new JsonArray();

      for (String line : linesList) {
        if(line.endsWith("," + username))
        {
          String csvLine = line.trim();
          String[] fields = csvLine.split(",");
          Order order = new Order();
          order.setOrderId(fields[0]);
          order.setOrderName(fields[1]);
          order.setOrderDate(fields[2]);
          order.setUsername(fields[3]);
          jsonArray.add(JsonObject.mapFrom(order));
        }
      }

      return jsonArray;

    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
      return new JsonArray();
    }
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
    }
  }
}
