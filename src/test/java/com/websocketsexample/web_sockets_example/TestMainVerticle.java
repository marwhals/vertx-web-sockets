package com.websocketsexample.web_sockets_example;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(TestMainVerticle.class);

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Test
  void can_connect_to_web_socket_server(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = vertx.createHttpClient();
    client.webSocket(new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8900)
        .setURI(WebSocketHandler.PATH)
      )
      .onFailure(testContext::failNow)
      .onComplete(testContext.succeeding(ws -> {
          ws.handler(data -> {
            final var receivedData = data.toString();
            LOG.debug("Recieved message: {}", receivedData);
            assertEquals("Connected to server web socket", receivedData);
            client.close();
            testContext.completeNow();
          });
        })
      );
  }

  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Test
  void can_recieve_multiple_messages(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = vertx.createHttpClient();
    final int EXPECTED_MESSAGES = 5;
    final AtomicInteger counter = new AtomicInteger(0);

    client.webSocket(8900, "localhost", WebSocketHandler.PATH)
      .onFailure(testContext::failNow)
      .onComplete(testContext.succeeding(ws -> {
          ws.handler(data -> {
            final var receivedData = data.toString();
            LOG.debug("Recieved message: {}", receivedData);
            var currentValue = counter.getAndIncrement();
            if (currentValue > EXPECTED_MESSAGES) {
              client.close();
              testContext.completeNow();
            } else {
              LOG.debug("Not enough messages - currently {} expect {}: ", currentValue, EXPECTED_MESSAGES);
            }
          });
        })
      );
  }
}
