package com.orders.pojos;

public class Order {
  private String orderId;
  private String orderName;
  private String orderDate;
  private String username;

  public Order() {
  }

  public Order(String orderId, String orderName, String orderDate, String username) {
    this.orderId = orderId;
    this.orderName = orderName;
    this.orderDate = orderDate;
    this.username = username;
  }

  // Getters and Setters

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderName() {
    return orderName;
  }

  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(String orderDate) {
    this.orderDate = orderDate;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return orderId + "," + orderName + "," + orderDate + "," + username  + "\n";
  }
}
