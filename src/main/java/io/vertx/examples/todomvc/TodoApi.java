package io.vertx.examples.todomvc;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.examples.todomvc.dao.TodoDAO;
import io.vertx.examples.todomvc.dao.impl.LocalMapDAO;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Arrays;
import java.util.HashSet;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.*;

import static io.vertx.examples.todomvc.dao.TodoDAO.StorageStrategy.*;

public class TodoApi implements Handler<HttpServerRequest> {

    private final static String PAYLOAD = "todo-payload"; // just to avoid making many search, keep a ref. to the Todo payload
    private final static String ID_PARAM = "todo-id";

    private final Router router;
    private final TodoDAO dao;

    public TodoApi(Vertx vertx) {
        dao = TodoDAO.create(vertx, MAP);
        router = Router.router(vertx);
        attachRoutes();
    }

    @Override
    public void handle(HttpServerRequest request) {
        router.accept(request);
    }

    private void attachRoutes() {
        CorsHandler cors = CorsHandler.create("*");
        cors.allowedHeader(CONTENT_TYPE.toString());
        cors.allowedMethods(new HashSet<>(Arrays.asList(GET, POST, PUT, PATCH, DELETE, OPTIONS)));
        router.route().handler(cors);
        router.route().handler(BodyHandler.create());

        router.get("/").handler(ctx -> {
            ctx.response().end(new JsonArray(dao.getTodos()).toString());
        });

        router.delete("/").handler(ctx -> {
            dao.clearTodos();
            ctx.response().end();
        });

        router.post("/").handler(this::checkRequestBody);
        router.post("/").handler(ctx -> {
            JsonObject todo =  dao.newTodo(ctx.getBodyAsJson());
            ctx.put(PAYLOAD, todo);
            ctx.next();
        });
        router.post("/").handler(this::sendPayload);


        router.get("/todos/:id").handler(this::checkId);
        router.get("/todos/:id").handler(this::sendPayload);

        router.put("/todos/:id").handler(this::checkId);
        router.put("/todos/:id").handler(this::checkRequestBody);
        router.put("/todos/:id").handler(this::updateTodo);
        router.put("/todos/:id").handler(this::sendPayload);

        router.patch("/todos/:id").handler(this::checkId);
        router.patch("/todos/:id").handler(this::checkRequestBody);
        router.patch("/todos/:id").handler(this::updateTodo);
        router.patch("/todos/:id").handler(this::sendPayload);

        router.delete("/todos/:id").handler(this::checkId);
        router.delete("/todos/:id").handler(ctx -> {
            dao.deleteTodo(ctx.get(ID_PARAM));
            ctx.response().end();
        });


    }

    private void checkRequestBody(RoutingContext context) {
        try {
            context.getBodyAsJson();
            context.next();
        } catch(Exception e) {
            context.response().setStatusCode(400);
            context.response().end("Expecting JSON");
        }
    }

    private void checkId(RoutingContext context) {
        int id;
        try {
            id = Integer.parseInt(context.request().getParam("id"));
            context.put(ID_PARAM, id);
        } catch(NumberFormatException nfe) {
            context.response().setStatusCode(400);
            context.response().end("Invalid Id");
            return;
        }
        JsonObject todo = dao.getTodo(id);
        if (todo == null) {
            context.response().setStatusCode(404);
            context.response().end("TODO not found");
            return;
        } else {
            context.put(PAYLOAD, todo);
        }
        context.next();
    }

    private void updateTodo(RoutingContext context) {
        JsonObject updated = dao.updateTodo(context.get(ID_PARAM), context.getBodyAsJson());
        context.put(PAYLOAD, updated);
        context.next();
    }

    private void sendPayload(RoutingContext context) {
        context.response().end(context.get(PAYLOAD).toString());
    }
}
