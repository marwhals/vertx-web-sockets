package com.websocketsexample.web_sockets_example;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler implements Handler<ServerWebSocket> {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
  public static final String PATH = "/websocket-example/simple/prices";
  private final PriceBroadcast broadcast;

  public WebSocketHandler(final Vertx vertx) {
    this.broadcast = new PriceBroadcast(vertx);
  }

  @Override
  public void handle(final ServerWebSocket serverWebSocket) {
    if (!PATH.equalsIgnoreCase(serverWebSocket.path())) {
      LOG.info("Incorrect path: {}", serverWebSocket.path());
      serverWebSocket.writeFinalTextFrame("Incorrect path. Only " + PATH + " is accepted");
      serverWebSocket.close((short) 1000, "Normal Closure");
      return;
    }

    LOG.info("Opening web socket connection: {} {}", serverWebSocket.path(), serverWebSocket.textHandlerID()); //Check NULL
    serverWebSocket.accept();
    serverWebSocket.frameHandler(receivedFrame -> frameHandler(serverWebSocket, receivedFrame));
    serverWebSocket.endHandler(onClose -> {
      LOG.info("Connection closed: {}", serverWebSocket.textHandlerID());
      broadcast.unregister(serverWebSocket);
  });
    serverWebSocket.exceptionHandler(err -> LOG.error("Connection failed: ", err));
    serverWebSocket.writeTextMessage("Connected to server web socket");
    broadcast.register(serverWebSocket);
  }

  private static void frameHandler(ServerWebSocket serverWebSocket, WebSocketFrame receivedFrame) {
    final String message = receivedFrame.textData();
    LOG.debug("Received message: {} from client {}", message, serverWebSocket.textHandlerID());

    if ("disconnect me".equalsIgnoreCase(message)) {
      LOG.debug("Client close requested");
      serverWebSocket.close((short) 1000, "Normal Closure");
    } else {
      serverWebSocket.writeTextMessage("Not supported => (" + message + ")");
    }
  }
}
