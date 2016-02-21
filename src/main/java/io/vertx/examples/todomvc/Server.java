package io.vertx.examples.todomvc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class Server extends AbstractVerticle {

    private HttpServer server;

    public final static int PORT = 9000;
    public final static String HOST = "localhost";

    @Override
    public void start(final Future<Void> future) {
        server = createServer();
        server.requestHandler(new TodoApi(vertx));
        server.listen(res -> {
           if (res.failed()) {
               future.fail(res.cause());
           } else {
               future.complete();
           }
        });
    }

    @Override
    public void stop(Future<Void> future) {
        if (server == null) {
            future.complete();
            return;
        }
        server.close(future.completer());
    }


    private HttpServer createServer() {
        HttpServerOptions options = new HttpServerOptions();
        options.setHost(HOST);
        options.setPort(PORT);
        return vertx.createHttpServer(options);
    }

}
