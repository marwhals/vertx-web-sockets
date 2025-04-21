package com.websocketsexample.web_sockets_example;


import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PriceBroadcast {

  private static final Logger LOG = LoggerFactory.getLogger(PriceBroadcast.class);
  private final Map<String, ServerWebSocket> connectedClients = new HashMap<>();

  public PriceBroadcast(final Vertx vertx) {
    periodicUpdate(vertx);
  }

  private void periodicUpdate(final Vertx vertx) {
    vertx.setPeriodic(Duration.ofSeconds(1).toMillis(), id -> {
      LOG.debug("Push update to {} clients!", connectedClients.size());
      final String priceUpdate = new JsonObject()
        .put("symbol", "XYZ")
        .put("value", new Random().nextInt(1000))
        .toString();
      connectedClients.values().forEach(serverWebSocket ->
        serverWebSocket.writeTextMessage(priceUpdate));
    });
  }

  public void register(final ServerWebSocket serverWebSocket) {
    connectedClients.put(serverWebSocket.textHandlerID(), serverWebSocket); //Id is unique so it can be used as the ID in the hashmap
  }

  public void unregister(final ServerWebSocket serverWebSocket) {
    connectedClients.remove(serverWebSocket.textHandlerID());
  }
}
