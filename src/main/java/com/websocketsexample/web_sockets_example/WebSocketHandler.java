package com.websocketsexample.web_sockets_example;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler implements Handler<ServerWebSocket> {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);

  @Override
  public void handle(final ServerWebSocket serverWebSocket) {
    LOG.info("Opening web socket connection: {} {}", serverWebSocket.path(), serverWebSocket.textHandlerID());
    serverWebSocket.accept();
    serverWebSocket.endHandler(onClose ->
      LOG.info("Connection closed: {}", serverWebSocket.textHandlerID()));
    serverWebSocket.exceptionHandler(err -> LOG.error("Connection failed: ", err));
    serverWebSocket.writeTextMessage("Connected to server web socket");

  }
}
