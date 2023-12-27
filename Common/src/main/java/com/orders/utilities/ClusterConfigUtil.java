package com.orders.utilities;

import com.hazelcast.config.Config;

import java.net.URL;

public class ClusterConfigUtil {

  public static Config loadConfig() {

    URL resourceUrl = ClusterConfigUtil.class.getClassLoader().getResource("hazelcast_config.xml");

    if (resourceUrl != null) {
      String resourceString = resourceUrl.toString();
      Config hazelcastConfig = new Config(resourceString);
      return hazelcastConfig;
    } else {
      // Handle the case where the resource is not found
      throw new RuntimeException("Hazelcast configuration file not found");
    }
  }
}
