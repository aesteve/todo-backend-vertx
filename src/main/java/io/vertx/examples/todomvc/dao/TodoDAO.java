package io.vertx.examples.todomvc.dao;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.examples.todomvc.dao.impl.LocalMapDAO;

import java.util.List;

/**
 * Basic CRUD operations, to be implemented using :
 * - a Vert.x shared map
 * - Mongo
 * - ...
 */
public interface TodoDAO {

    public enum StorageStrategy {
        MAP
    }

    public static TodoDAO create(Vertx vertx, StorageStrategy strategy) {
        switch (strategy) {
            case MAP:
                return new LocalMapDAO(vertx);
            default:
                throw new UnsupportedOperationException("Unknown storage strategy " + strategy);
        }
    }

    public List<JsonObject> getTodos();

    public JsonObject getTodo(Integer id);

    public JsonObject updateTodo(Integer id, JsonObject diff);

    public JsonObject deleteTodo(Integer id);

    public JsonObject newTodo(JsonObject todo);

    public void clearTodos();

}
